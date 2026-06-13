package com.example.a220945_kasthoori_nelson_project2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.a220945_kasthoori_nelson_project2.data.FirebaseRepository
import com.example.a220945_kasthoori_nelson_project2.data.LeaderboardEntry
import com.example.a220945_kasthoori_nelson_project2.data.OverpassElement
import com.example.a220945_kasthoori_nelson_project2.data.OverpassRetrofit
import com.example.a220945_kasthoori_nelson_project2.data.local.CheckInRecord
import com.example.a220945_kasthoori_nelson_project2.data.local.EduQuestDatabase
import com.example.a220945_kasthoori_nelson_project2.data.local.UserProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

// ---------------------------------------------------------------------------
// DATA CLASSES
// ---------------------------------------------------------------------------

data class UserProfile(
    val name: String = "",
    val matricNumber: String = "",
    val program: String = "",
    val totalXP: Int = 0,
    val currentStreak: Int = 0,
    val lastActiveDate: String = "",
    val lessonHighScores: Map<String, Int> = emptyMap()
) {
    val currentTitle: String
        get() = when (totalXP) {
            in 0..499 -> "Novice Coder"
            in 500..999 -> "Logic Apprentice"
            in 1000..2499 -> "System Architect"
            else -> "Software Grandmaster"
        }
}

data class Course(
    val id: String,
    val title: String,
    val isCore: Boolean,
    val description: String,
    val progress: Float = 0f
)

data class CustomQuest(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val xpReward: Int
)

data class QuizQuestion(val text: String, val options: List<String>, val correctIndex: Int)

sealed class StartDestination {
    object Loading : StartDestination()
    object ProfileSetup : StartDestination()
    object Dashboard : StartDestination()
}

// ---------------------------------------------------------------------------
// VIEWMODEL
// ---------------------------------------------------------------------------

class EduQuestViewModel(application: Application) : AndroidViewModel(application) {

    private val db = EduQuestDatabase.getDatabase(application)
    private val checkInDao = db.checkInDao()
    private val userProfileDao = db.userProfileDao()

    // SharedPreferences stores the last logged-in matric number so we know
    // exactly which user to restore after a force close
    private val prefs = application.getSharedPreferences("eduquest_prefs", android.content.Context.MODE_PRIVATE)
    private val PREF_LAST_MATRIC = "last_matric"
    val checkInHistory = checkInDao.getAllCheckIns()

    private val firebaseRepository = FirebaseRepository()

    private val _startDestination = MutableStateFlow<StartDestination>(StartDestination.Loading)
    val startDestination: StateFlow<StartDestination> = _startDestination.asStateFlow()

    private val _uiState = MutableStateFlow(UserProfile())
    val uiState: StateFlow<UserProfile> = _uiState.asStateFlow()

    private val _selectedQuest = MutableStateFlow("")
    val selectedQuest: StateFlow<String> = _selectedQuest.asStateFlow()

    private val _studySpots = MutableStateFlow<List<OverpassElement>>(emptyList())
    val studySpots: StateFlow<List<OverpassElement>> = _studySpots.asStateFlow()

    private val _studySpotsLoading = MutableStateFlow(false)
    val studySpotsLoading: StateFlow<Boolean> = _studySpotsLoading.asStateFlow()

    private val _studySpotsError = MutableStateFlow("")
    val studySpotsError: StateFlow<String> = _studySpotsError.asStateFlow()

    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    private val _leaderboardLoading = MutableStateFlow(false)
    val leaderboardLoading: StateFlow<Boolean> = _leaderboardLoading.asStateFlow()

    private val _leaderboardError = MutableStateFlow("")
    val leaderboardError: StateFlow<String> = _leaderboardError.asStateFlow()

    // ---------------------------------------------------------------------------
    // COURSES — per-program core courses + shared Citra courses
    // ---------------------------------------------------------------------------

    private val citraCourses = listOf(
        Course("LMCR1252", "Pronunciation in English", false, "Master phonetics, intonation, and rhythm. Intensive speaking drills and audio analysis.", 0f),
        Course("LMCR2482", "Basic Graphic Design", false, "Color theory, typography, and layout. Create digital posters, logos, and vector illustrations.", 0f),
        Course("LMCP1012", "Intro to Liberal Studies", false, "Intersection of history, philosophy, and sociology. Critical thinking on global cultural shifts.", 0f),
        Course("LMCR2322", "Health and Environment", false, "Pollution, climate change, and urbanization effects on public health. Malaysian case studies.", 0f),
        Course("LMCR1102", "Volunteerism & Social Service", false, "Ethics of community engagement. Plan and execute a real-world social impact project.", 0f),
        Course("LMCS1672", "Basic Table Tennis", false, "Hand-eye coordination, footwork, and serving techniques. Tournament rules and sports psychology.", 0f)
    )

    private val seCoreCourses = listOf(
        Course("MAD", "Mobile Application Development", true, "Basic UI, Interaction, and Material Design.", 0f),
        Course("SD", "Software Design", true, "Design principles, techniques, methods, and interface design.", 0f),
        Course("SQM", "Software Quality & Management", true, "Project planning, quality culture, and software standards.", 0f),
        Course("UX", "User Experience Design", true, "UX principles, user needs, and digital interfaces (Topics 1-10).", 0f)
    )

    private val csCoreCourses = listOf(
        Course("TTTK2313", "Artificial Intelligence", true, "Intelligent agents, search algorithms, knowledge representation, and machine learning basics.", 0f),
        Course("TTTK1233", "Object-Oriented Programming", true, "OOP concepts: classes, inheritance, polymorphism, encapsulation, and abstraction.", 0f),
        Course("TTTK2173", "Data Structures and Algorithms", true, "Arrays, linked lists, trees, graphs, sorting, and searching algorithms.", 0f),
        Course("TTTK2223", "Theory of Computer Science", true, "Automata theory, formal languages, computability, and complexity theory.", 0f)
    )

    private val itCoreCourses = listOf(
        Course("TTTK1153", "Computer Organization and Architecture", true, "CPU design, memory hierarchy, instruction sets, and I/O systems.", 0f),
        Course("TTTK1113", "Computer Programming", true, "Fundamentals of programming: variables, loops, functions, and file handling.", 0f),
        Course("TTTK1143", "Program Design and Problem Solving", true, "Algorithmic thinking, flowcharts, pseudocode, and structured programming.", 0f),
        Course("TTTU2983", "Advanced Database", true, "Relational model, SQL, normalization, transactions, and database security.", 0f)
    )

    // Returns the correct core courses based on the current user's program
    private fun getCoreCourses(program: String): List<Course> = when (program) {
        "Computer Science" -> csCoreCourses
        "Information Technology" -> itCoreCourses
        else -> seCoreCourses // Default = Software Engineering
    }

    private val _allCourses = MutableStateFlow<List<Course>>(emptyList())
    val allCourses: StateFlow<List<Course>> = _allCourses.asStateFlow()

    // Rebuilds the course list, restoring saved progress if available
    private fun rebuildCourses(program: String, savedProgress: Map<String, Float> = emptyMap()) {
        val core = getCoreCourses(program).map { course ->
            course.copy(progress = savedProgress[course.id] ?: 0f)
        }
        val citra = citraCourses.map { course ->
            course.copy(progress = savedProgress[course.id] ?: 0f)
        }
        _allCourses.value = core + citra
    }

    // Collects current course progress into a Map for saving to Room
    private fun getCurrentCourseProgress(): Map<String, Float> {
        return _allCourses.value.associate { it.id to it.progress }
    }

    // ---------------------------------------------------------------------------
    // QUIZ DATA — all four programs have quiz questions
    // ---------------------------------------------------------------------------

    val courseQuizzes: Map<String, Map<String, List<QuizQuestion>>> = mapOf(
        // --- Software Engineering ---
        "MAD" to mapOf(
            "Lesson 1" to listOf(
                QuizQuestion("Which function is used to remember state?", listOf("Modifier.padding()", "remember { mutableStateOf() }", "NavHost()"), 1),
                QuizQuestion("What manages moving between screens?", listOf("NavController", "Card", "Button"), 0),
                QuizQuestion("What is a composable?", listOf("A UI element", "A database", "A server file"), 0)
            ),
            "Lesson 2" to listOf(
                QuizQuestion("What does ViewModel survive?", listOf("Phone calls", "App uninstalls", "Configuration changes/rotations"), 2),
                QuizQuestion("What handles live data streams?", listOf("StateFlow", "StaticText", "XML"), 0),
                QuizQuestion("How do you add space in Compose?", listOf("<br>", "Spacer()", "Margin()"), 1)
            )
        ),
        "SD" to mapOf(
            "Lesson 1" to listOf(
                QuizQuestion("What does SOLID stand for?", listOf("A design principle", "A database type", "A UI color"), 0),
                QuizQuestion("What is a UML diagram?", listOf("A visual model of a system", "A programming language", "A server"), 0),
                QuizQuestion("Which is a structural pattern?", listOf("Observer", "Singleton", "Adapter"), 2)
            ),
            "Lesson 2" to listOf(
                QuizQuestion("What does API stand for?", listOf("Application Programming Interface", "Apple Product Index", "Android Process Interactor"), 0),
                QuizQuestion("What is coupling?", listOf("Degree of interdependence", "Joining databases", "UI design"), 0),
                QuizQuestion("What is cohesion?", listOf("How strongly related elements are within a module", "Network speed", "Disk space"), 0)
            )
        ),
        "SQM" to mapOf(
            "Lesson 1" to listOf(
                QuizQuestion("What is Quality Assurance?", listOf("Testing software", "Preventing defects", "Writing code"), 1),
                QuizQuestion("What is a bug report?", listOf("A code error document", "A feature request", "A design file"), 0),
                QuizQuestion("Which testing is done by users?", listOf("Unit Testing", "UAT (User Acceptance Testing)", "Integration Testing"), 1)
            ),
            "Lesson 2" to listOf(
                QuizQuestion("What is Agile?", listOf("A waterfall method", "An iterative approach", "A programming language"), 1),
                QuizQuestion("What is a Scrum Master?", listOf("A boss", "A facilitator for an agile team", "A developer"), 1),
                QuizQuestion("What is a sprint?", listOf("A short, time-boxed period", "A database query", "A fast code compiler"), 0)
            )
        ),
        "UX" to mapOf(
            "Lesson 1" to listOf(
                QuizQuestion("What is User Persona?", listOf("A fictional character representing a user type", "A real user", "The app developer"), 0),
                QuizQuestion("What does Wireframing mean?", listOf("Connecting cables", "Creating a basic visual guide", "Writing backend logic"), 1),
                QuizQuestion("What is A/B testing?", listOf("Testing two versions to see which performs better", "A grading system", "Testing alphabet inputs"), 0)
            ),
            "Lesson 2" to listOf(
                QuizQuestion("What is accessibility?", listOf("Making apps usable for people with disabilities", "App download speed", "Server uptime"), 0),
                QuizQuestion("What is a Call to Action (CTA)?", listOf("A prompt urging the user to take action", "A phone call", "An error message"), 0),
                QuizQuestion("Which color contrast is best for reading?", listOf("Light gray on white", "Black on white", "Neon green on yellow"), 1)
            )
        ),
        // --- Computer Science ---
        "TTTK2313" to mapOf(
            "Lesson 1" to listOf(
                QuizQuestion("What is a rational agent in AI?", listOf("An agent that always wins", "An agent that acts to maximise its performance measure", "An agent that uses random actions"), 1),
                QuizQuestion("Which search algorithm uses a priority queue?", listOf("DFS", "BFS", "A* Search"), 2),
                QuizQuestion("What does ML stand for?", listOf("Memory Logic", "Machine Learning", "Model Language"), 1)
            ),
            "Lesson 2" to listOf(
                QuizQuestion("What is a neural network inspired by?", listOf("The human brain", "A computer chip", "A database"), 0),
                QuizQuestion("What is supervised learning?", listOf("Learning without labels", "Learning from labelled data", "Learning by reward"), 1),
                QuizQuestion("What is overfitting?", listOf("Model too simple", "Model performs well on training but poorly on new data", "Model has too few layers"), 1)
            )
        ),
        "TTTK1233" to mapOf(
            "Lesson 1" to listOf(
                QuizQuestion("What is encapsulation?", listOf("Hiding internal data and exposing only what is needed", "Inheriting from a parent class", "Creating multiple objects"), 0),
                QuizQuestion("What keyword creates a class in Java/Kotlin?", listOf("object", "class", "struct"), 1),
                QuizQuestion("What is polymorphism?", listOf("One interface, many implementations", "Copying an object", "Deleting a class"), 0)
            ),
            "Lesson 2" to listOf(
                QuizQuestion("What is inheritance?", listOf("A child class acquiring properties of a parent", "Two classes merging", "A method calling itself"), 0),
                QuizQuestion("What is an abstract class?", listOf("A class with no methods", "A class that cannot be instantiated directly", "A class with only static methods"), 1),
                QuizQuestion("What does 'override' do in Kotlin?", listOf("Deletes a method", "Replaces a parent class method with a new implementation", "Copies a method"), 1)
            )
        ),
        "TTTK2173" to mapOf(
            "Lesson 1" to listOf(
                QuizQuestion("What is the time complexity of binary search?", listOf("O(n)", "O(log n)", "O(n²)"), 1),
                QuizQuestion("Which data structure uses LIFO?", listOf("Queue", "Stack", "Linked List"), 1),
                QuizQuestion("What is a linked list?", listOf("A table of values", "A sequence of nodes where each points to the next", "A sorted array"), 1)
            ),
            "Lesson 2" to listOf(
                QuizQuestion("What is a binary tree?", listOf("A tree where each node has at most 2 children", "A tree with exactly 2 levels", "A sorted list"), 0),
                QuizQuestion("Which sorting algorithm has O(n log n) average time?", listOf("Bubble Sort", "Insertion Sort", "Merge Sort"), 2),
                QuizQuestion("What is a graph?", listOf("A chart", "A set of nodes connected by edges", "A matrix"), 1)
            )
        ),
        "TTTK2223" to mapOf(
            "Lesson 1" to listOf(
                QuizQuestion("What is a finite automaton?", listOf("A machine with infinite states", "A machine with a finite number of states that processes input", "A type of neural network"), 1),
                QuizQuestion("What does DFA stand for?", listOf("Dynamic Function Algorithm", "Deterministic Finite Automaton", "Digital File Access"), 1),
                QuizQuestion("What is a regular language?", listOf("A language spoken daily", "A language recognised by a finite automaton", "A programming language"), 1)
            ),
            "Lesson 2" to listOf(
                QuizQuestion("What is the Halting Problem?", listOf("A sorting problem", "The problem of determining if a program will halt", "A memory allocation problem"), 1),
                QuizQuestion("What is a Turing Machine?", listOf("A physical computer", "A theoretical model of computation", "A type of compiler"), 1),
                QuizQuestion("What is NP-Complete?", listOf("A problem solvable in polynomial time", "A problem verifiable in polynomial time but not known to be solvable quickly", "An unsolvable problem"), 1)
            )
        ),
        // --- Information Technology ---
        "TTTK1153" to mapOf(
            "Lesson 1" to listOf(
                QuizQuestion("What does CPU stand for?", listOf("Central Processing Unit", "Computer Power Unit", "Control Processing Utility"), 0),
                QuizQuestion("What is cache memory?", listOf("Long-term storage", "Fast memory between CPU and RAM", "External hard drive"), 1),
                QuizQuestion("What is the fetch-decode-execute cycle?", listOf("A network protocol", "The basic operation cycle of a CPU", "A file compression method"), 1)
            ),
            "Lesson 2" to listOf(
                QuizQuestion("What is pipelining in CPUs?", listOf("A data transfer protocol", "Overlapping instruction execution stages", "A type of memory"), 1),
                QuizQuestion("What does RAM stand for?", listOf("Random Access Memory", "Read-Only Access Module", "Rapid Algorithm Memory"), 0),
                QuizQuestion("What is an instruction set?", listOf("A set of rules", "The collection of commands a CPU can execute", "A software package"), 1)
            )
        ),
        "TTTK1113" to mapOf(
            "Lesson 1" to listOf(
                QuizQuestion("What is a variable?", listOf("A fixed value", "A named storage location for data", "A type of loop"), 1),
                QuizQuestion("What is a function?", listOf("A reusable block of code", "A data type", "A loop"), 0),
                QuizQuestion("What does 'if-else' do?", listOf("Loops through a list", "Makes a decision based on a condition", "Defines a variable"), 1)
            ),
            "Lesson 2" to listOf(
                QuizQuestion("What is a loop?", listOf("A function call", "A block of code that repeats", "A data structure"), 1),
                QuizQuestion("What is recursion?", listOf("A loop that counts down", "A function that calls itself", "A type of array"), 1),
                QuizQuestion("What is a syntax error?", listOf("A logic mistake", "An error in the structure of the code", "A runtime crash"), 1)
            )
        ),
        "TTTK1143" to mapOf(
            "Lesson 1" to listOf(
                QuizQuestion("What is an algorithm?", listOf("A programming language", "A step-by-step procedure to solve a problem", "A type of database"), 1),
                QuizQuestion("What is a flowchart?", listOf("A chart of data values", "A visual diagram of an algorithm's steps", "A network diagram"), 1),
                QuizQuestion("What is pseudocode?", listOf("Broken code", "An informal high-level description of an algorithm", "Encrypted code"), 1)
            ),
            "Lesson 2" to listOf(
                QuizQuestion("What is top-down design?", listOf("Breaking a problem into smaller sub-problems", "Writing code from bottom to top", "A sorting technique"), 0),
                QuizQuestion("What is a test case?", listOf("A hardware component", "A specific scenario used to verify program correctness", "A programming loop"), 1),
                QuizQuestion("What does debugging mean?", listOf("Writing new features", "Finding and fixing errors in code", "Compiling code"), 1)
            )
        ),
        "TTTU2983" to mapOf(
            "Lesson 1" to listOf(
                QuizQuestion("What is a primary key?", listOf("A password", "A unique identifier for a record in a table", "A foreign table"), 1),
                QuizQuestion("What does SQL stand for?", listOf("Structured Query Language", "Simple Question Logic", "System Query Link"), 0),
                QuizQuestion("What is normalization?", listOf("Deleting duplicate rows", "Organising data to reduce redundancy", "Sorting a table"), 1)
            ),
            "Lesson 2" to listOf(
                QuizQuestion("What is a transaction?", listOf("A database backup", "A sequence of operations treated as a single unit", "A query result"), 1),
                QuizQuestion("What does ACID stand for?", listOf("Atomicity, Consistency, Isolation, Durability", "Access, Control, Index, Data", "Aggregate, Combine, Insert, Delete"), 0),
                QuizQuestion("What is a foreign key?", listOf("A key from another country", "A field that links to a primary key in another table", "An encrypted key"), 1)
            )
        )
    )

    // ---------------------------------------------------------------------------
    // INIT — check Room for any previously logged-in user
    // ---------------------------------------------------------------------------

    init {
        rebuildCourses("Software Engineering") // Default until profile loads
        viewModelScope.launch {
            // Read the last logged-in matric from SharedPreferences.
            // This is set every time a user logs in via updateProfile().
            val lastMatric = prefs.getString(PREF_LAST_MATRIC, null)
            if (!lastMatric.isNullOrEmpty()) {
                val saved = userProfileDao.getProfileByMatric(lastMatric)
                if (saved != null) {
                    _uiState.value = saved.toUserProfile()
                    rebuildCourses(saved.program, saved.getCourseProgress())
                    _startDestination.value = StartDestination.Dashboard
                } else {
                    _startDestination.value = StartDestination.ProfileSetup
                }
            } else {
                _startDestination.value = StartDestination.ProfileSetup
            }
        }
    }

    // ---------------------------------------------------------------------------
    // PROFILE — login loads existing data, setup creates new
    // ---------------------------------------------------------------------------

    /**
     * Called when user taps "Enter Dashboard" on ProfileSetup.
     * If their matric number exists in Room, loads their saved data.
     * If new, creates a fresh profile and saves it.
     */
    fun updateProfile(newName: String, newMatric: String, newProgram: String) {
        viewModelScope.launch {
            val existing = userProfileDao.getProfileByMatric(newMatric)
            if (existing != null) {
                // Returning user — restore their saved XP, streak, high scores
                val restored = existing.toUserProfile()
                _uiState.value = restored
                rebuildCourses(restored.program, existing.getCourseProgress())
            } else {
                // New user — create fresh profile
                val fresh = UserProfile(
                    name = newName,
                    matricNumber = newMatric,
                    program = newProgram
                )
                _uiState.value = fresh
                rebuildCourses(newProgram)
                userProfileDao.saveProfile(fresh.toEntity())
            }
            // Save matric to SharedPreferences so force-close restores this user
            prefs.edit().putString(PREF_LAST_MATRIC, newMatric).apply()
            // Sync current XP to Firebase so leaderboard always reflects Room data
            pushToLeaderboard()
        }
    }

    /**
     * LOGOUT — clears in-memory session only. Room data is kept intact.
     * Next time this matric number logs in, all progress is restored.
     */
    fun logoutUser() {
        // Clear last matric so next launch shows ProfileSetup
        prefs.edit().remove(PREF_LAST_MATRIC).apply()
        _uiState.value = UserProfile()
        _selectedQuest.value = ""
        _customQuests.value = emptyList()
        _studySpots.value = emptyList()
        _leaderboard.value = emptyList()
        rebuildCourses("Software Engineering")
        _startDestination.value = StartDestination.ProfileSetup
    }

    /**
     * RESET — permanently deletes current user's data from Room.
     * All XP, check-ins, and progress are gone. Cannot be undone.
     */
    fun resetCurrentUser() {
        val matric = _uiState.value.matricNumber
        val name = _uiState.value.name
        viewModelScope.launch {
            if (matric.isNotEmpty()) {
                userProfileDao.deleteProfile(matric)
                checkInDao.clearAll()
                // Push zeroed entry to Firebase so leaderboard reflects the reset
                firebaseRepository.pushToLeaderboard(
                    LeaderboardEntry(
                        name = name,
                        matricNumber = matric,
                        totalXP = 0,
                        currentTitle = "Novice Coder",
                        checkInCount = 0
                    )
                )
            }
        }
        logoutUser()
    }

    fun selectQuest(questId: String) { _selectedQuest.value = questId }

    // ---------------------------------------------------------------------------
    // REST API — widened to 10km radius
    // ---------------------------------------------------------------------------

    fun fetchStudySpots(lat: Double, lon: Double) {
        viewModelScope.launch {
            _studySpotsLoading.value = true
            _studySpotsError.value = ""
            try {
                // Build query using string concatenation to avoid quote escaping issues
                val q1 = "[out:json][timeout:25];("
                val q2 = "node[amenity~'library|cafe|restaurant|university|college'](around:10000,$lat,$lon);"
                val q3 = "node[leisure~'park'](around:10000,$lat,$lon);"
                val q4 = "node[shop~'books'](around:10000,$lat,$lon);"
                val q5 = ");out body 20;"
                val query = q1 + q2 + q3 + q4 + q5
                val response = OverpassRetrofit.service.searchNearby(query)
                _studySpots.value = response.elements
                    .filter { it.lat != null && it.lon != null }
                    .take(20)
                if (_studySpots.value.isEmpty()) {
                    _studySpotsError.value = "No study spots found nearby. Try from a different location."
                }
            } catch (e: Exception) {
                _studySpotsError.value = "Could not load study spots. Check your internet connection."
                _studySpots.value = emptyList()
            } finally {
                _studySpotsLoading.value = false
            }
        }
    }

    // ---------------------------------------------------------------------------
    // FIREBASE
    // ---------------------------------------------------------------------------

    fun pushToLeaderboard() {
        val profile = _uiState.value
        if (profile.name.isEmpty()) return
        viewModelScope.launch {
            // Get the real total check-in count from Room for this student
            val totalCheckIns = checkInDao.countTotalCheckIns(profile.matricNumber)
            firebaseRepository.pushToLeaderboard(
                LeaderboardEntry(
                    name = profile.name,
                    matricNumber = profile.matricNumber,
                    totalXP = profile.totalXP,
                    currentTitle = profile.currentTitle,
                    checkInCount = totalCheckIns
                )
            )
        }
    }

    fun fetchLeaderboard() {
        viewModelScope.launch {
            _leaderboardLoading.value = true
            _leaderboardError.value = ""
            val result = firebaseRepository.getLeaderboard()
            result.onSuccess { entries ->
                _leaderboard.value = entries
                if (entries.isEmpty()) _leaderboardError.value = "No students on the leaderboard yet. Be the first!"
            }
            result.onFailure {
                _leaderboardError.value = "Could not load leaderboard. Check your internet connection."
            }
            _leaderboardLoading.value = false
        }
    }

    // ---------------------------------------------------------------------------
    // ROOM — check-in
    // ---------------------------------------------------------------------------

    fun saveCheckIn(latitude: Double, longitude: Double, locationName: String, xpEarned: Int) {
        val profile = _uiState.value
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
            val startOfDay = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59)
            val endOfDay = calendar.timeInMillis

            val checkInsToday = checkInDao.countCheckInsToday(profile.matricNumber, startOfDay, endOfDay)
            if (checkInsToday == 0) {
                checkInDao.insertCheckIn(
                    CheckInRecord(
                        studentName = profile.name,
                        matricNumber = profile.matricNumber,
                        latitude = latitude,
                        longitude = longitude,
                        locationName = locationName,
                        xpEarned = xpEarned
                    )
                )
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayDate = sdf.format(Date())
                val newStreak = calculateStreak(profile.currentStreak, profile.lastActiveDate, todayDate, sdf)
                val updated = profile.copy(totalXP = profile.totalXP + xpEarned, currentStreak = newStreak, lastActiveDate = todayDate)
                _uiState.value = updated
                userProfileDao.saveProfile(updated.toEntity())
                pushToLeaderboard()
                // Mark as checked in immediately so the button disables right away
                _alreadyCheckedInToday.value = true
            }
        }
    }

    private val _alreadyCheckedInToday = MutableStateFlow(false)
    val alreadyCheckedInToday: StateFlow<Boolean> = _alreadyCheckedInToday.asStateFlow()

    fun checkIfAlreadyCheckedIn() {
        val profile = _uiState.value
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
            val startOfDay = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59)
            val endOfDay = calendar.timeInMillis
            val count = checkInDao.countCheckInsToday(profile.matricNumber, startOfDay, endOfDay)
            _alreadyCheckedInToday.value = count > 0
        }
    }

    // ---------------------------------------------------------------------------
    // QUIZ + READING
    // ---------------------------------------------------------------------------

    fun completeQuiz(courseId: String, lessonId: String, score: Int) {
        val p = _uiState.value
        val previousHighScore = p.lessonHighScores[lessonId] ?: 0
        val newXPEarned = if (score > previousHighScore) (score - previousHighScore) * 50 else 0
        val updatedHighScores = p.lessonHighScores.toMutableMap()
        if (score > previousHighScore) updatedHighScores[lessonId] = score

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = sdf.format(Date())
        val newStreak = calculateStreak(p.currentStreak, p.lastActiveDate, todayDate, sdf)

        val updated = p.copy(totalXP = p.totalXP + newXPEarned, currentStreak = newStreak, lastActiveDate = todayDate, lessonHighScores = updatedHighScores)
        _uiState.value = updated

        val progressEarned = if (score > previousHighScore) ((score - previousHighScore).toFloat() / 6f) else 0f
        _allCourses.value = _allCourses.value.map { course ->
            if (course.id == courseId) course.copy(progress = (course.progress + progressEarned).coerceAtMost(1f))
            else course
        }
        // Save profile AND course progress together to Room
        viewModelScope.launch { userProfileDao.saveProfile(updated.toEntity(getCurrentCourseProgress())) }
    }

    fun completeReading(courseId: String) {
        val currentCourse = _allCourses.value.find { it.id == courseId }
        if (currentCourse != null && currentCourse.progress < 1f) {
            val p = _uiState.value
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayDate = sdf.format(Date())
            val newStreak = calculateStreak(p.currentStreak, p.lastActiveDate, todayDate, sdf)
            val updated = p.copy(totalXP = p.totalXP + 50, currentStreak = newStreak, lastActiveDate = todayDate)
            _uiState.value = updated
            _allCourses.value = _allCourses.value.map { if (it.id == courseId) it.copy(progress = 1f) else it }
            // Save profile AND updated course progress together
            viewModelScope.launch { userProfileDao.saveProfile(updated.toEntity(getCurrentCourseProgress())) }
        }
    }

    // ---------------------------------------------------------------------------
    // CUSTOM QUESTS
    // ---------------------------------------------------------------------------

    private val _customQuests = MutableStateFlow<List<CustomQuest>>(emptyList())
    val customQuests: StateFlow<List<CustomQuest>> = _customQuests.asStateFlow()

    fun addCustomQuest(title: String, xp: Int) {
        _customQuests.value = _customQuests.value + CustomQuest(id = UUID.randomUUID().toString(), title = title, xpReward = xp)
    }

    fun completeCustomQuest(quest: CustomQuest) {
        val p = _uiState.value
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = sdf.format(Date())
        val newStreak = calculateStreak(p.currentStreak, p.lastActiveDate, todayDate, sdf)
        val updated = p.copy(totalXP = p.totalXP + quest.xpReward, currentStreak = newStreak, lastActiveDate = todayDate)
        _uiState.value = updated
        viewModelScope.launch { userProfileDao.saveProfile(updated.toEntity()) }
        _customQuests.value = _customQuests.value.filter { it.id != quest.id }
    }

    // ---------------------------------------------------------------------------
    // PRIVATE HELPERS
    // ---------------------------------------------------------------------------

    private fun calculateStreak(currentStreak: Int, lastActiveDate: String, todayDate: String, sdf: SimpleDateFormat): Int {
        if (lastActiveDate == todayDate) return currentStreak
        return try {
            val last = sdf.parse(lastActiveDate)
            val today = sdf.parse(todayDate)
            if (last != null && today != null) {
                val diffDays = ((today.time - last.time) / (1000 * 60 * 60 * 24)).toInt()
                if (diffDays == 1) currentStreak + 1 else 1
            } else 1
        } catch (e: Exception) { 1 }
    }
}

// ---------------------------------------------------------------------------
// EXTENSION FUNCTIONS
// ---------------------------------------------------------------------------

fun UserProfile.toEntity(courseProgress: Map<String, Float> = emptyMap()): UserProfileEntity {
    val scoresJson = buildString {
        append("{")
        lessonHighScores.entries.forEachIndexed { i, entry ->
            if (i > 0) append(",")
            append("\"${entry.key}\":${entry.value}")
        }
        append("}")
    }
    val progressJson = buildString {
        append("{")
        courseProgress.entries.forEachIndexed { i, entry ->
            if (i > 0) append(",")
            append("\"${entry.key}\":${entry.value}")
        }
        append("}")
    }
    return UserProfileEntity(
        matricNumber = matricNumber,
        name = name,
        program = program,
        totalXP = totalXP,
        currentStreak = currentStreak,
        lastActiveDate = lastActiveDate,
        lessonHighScoresJson = scoresJson,
        courseProgressJson = progressJson
    )
}

fun UserProfileEntity.toUserProfile(): UserProfile {
    val scoresMap = mutableMapOf<String, Int>()
    try {
        val json = JSONObject(lessonHighScoresJson)
        json.keys().forEach { key -> scoresMap[key] = json.getInt(key) }
    } catch (e: Exception) { }
    return UserProfile(
        name = name,
        matricNumber = matricNumber,
        program = program,
        totalXP = totalXP,
        currentStreak = currentStreak,
        lastActiveDate = lastActiveDate,
        lessonHighScores = scoresMap
    )
}

fun UserProfileEntity.getCourseProgress(): Map<String, Float> {
    val progressMap = mutableMapOf<String, Float>()
    try {
        val json = JSONObject(courseProgressJson)
        json.keys().forEach { key -> progressMap[key] = json.getDouble(key).toFloat() }
    } catch (e: Exception) { }
    return progressMap
}