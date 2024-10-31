package com.example.noteapphenry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.noteapphenry.ui.theme.NoteAppHenryTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

data class Note(
    val id: Int,
    val title: String,
    val description: String,
    val date: String
)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteAppHenryTheme {
                AppStarterNav()
            }
        }
    }
}

@Composable
fun AppStarterNav() { // starts the app and controll the navigation
    val navController = rememberNavController() // saves into compose
    val noteList = remember { mutableStateListOf<Note>() }  // makes a list and , compose updates the list when changes are done

    NavHost(navController = navController, startDestination = "start") { // defines the structure of the nav, startdestination decides where to land when start the app
        composable("start") { StartScreen(navController = navController, noteList = noteList) } //defines route for start, with startscreen, runs the startscreem with argumenst
        composable("addNote") { ModifyNoteScreen(navController = navController, noteList = noteList, noteId = null, isUpdating = false) } //addMote route. runs the modifyScreen function with arguments , no need for the node Id here or we are not going to uppdate anything
        composable("updateNote/{noteId}") { backStackEntry -> // update route, backstackentry to handle information aout the actual navigation
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() // if note it does not exist it  will be null
            ModifyNoteScreen(navController = navController, noteList = noteList, noteId = noteId, isUpdating = true) // runs the modifyNoteScreen with noteId and isuppdating
        }
        composable("noteDetail/{noteId}") { backStackEntry -> // route to show specific note
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() // if we found it converte to int, else null
            noteId?.let {
                NoteDetailScreen(navController = navController, noteList = noteList, noteId = noteId)// runs notedetailscreen with our id
            }
        }
    }
}
//shows the startscreen
@OptIn(ExperimentalMaterial3Api::class) //  needed it for topbar
@Composable
fun StartScreen(navController: NavController, noteList: MutableList<Note>) { // takes our navcontroller and list of notes, need to view them
    Scaffold( // like a layoutsctructure
        topBar = { // sets out topbar
            TopAppBar(
                title = { Text("Anteckningar:") }
            )
        }
    ) {
        Column( // a column in scaffold , define the color of the app, we want it to fill max size,
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize()
                .padding(start = 11.dp, top = 89.dp, end = 11.dp, bottom = 8.dp) // Manually padding
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(noteList) { note -> // for every note
                    Card( // we want a card
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(9.dp)
                            .clickable {
                                navController.navigate("noteDetail/${note.id}") // with right id
                            }
                    ) {
                        Row( // to arrange the text in the card
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),

                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column( // to show the title over the desc
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = note.title, style = MaterialTheme.typography.titleMedium) // want the title and date in startscreen and desc if u click on it
                                Text(text = note.date, style = MaterialTheme.typography.bodyMedium)
                            }
                            Image(
                                painter = painterResource(id = R.drawable.baseline_checklist_24), // tried drawable to inport icons
                                contentDescription = "Note icon",
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }
                }
            }
            Button( // the adnote btn
                onClick = { navController.navigate("addNote") },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B7355)) // exprimented with hex
            ) {
                Text(text = "NY ANTECKNING", fontFamily = FontFamily.Cursive) // could have been used an icon but did with text instead
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyNoteScreen(navController: NavController, noteList: MutableList<Note>, noteId: Int?, isUpdating: Boolean) {
    Scaffold(
        topBar = { // TOPBAR
            TopAppBar(
                title = { Text(if (isUpdating) "Uppdatera Anteckning" else "Lägg till Anteckning") },// if isupdating is true we will run uppdater instead of läg till
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // en icon , om vi klickar på den så poppar vi stacken och kommer till föregående sida
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Tillbaka")
                    }
                }
            )
        }
    ) { perfectPadding -> // a way for dynamic padding
        ModifyNoteContent(
            navController = navController,
            noteList = noteList,
            noteId = noteId,
            isUpdating = isUpdating,
            modifier = Modifier.padding(perfectPadding)
        )
    }
}
@Composable
// to create or update
fun ModifyNoteContent(navController: NavController, noteList: MutableList<Note>, noteId: Int?, isUpdating: Boolean, modifier: Modifier = Modifier) {
    val note = noteId?.let { noteList.find { it.id == noteId } } // find right id if update is true

    var title by remember { mutableStateOf(note?.title ?: "") } // gets the value of the note if exist or " " create a new
    var description by remember { mutableStateOf(note?.description ?: "") }
    var date by remember { mutableStateOf(note?.date ?: "") }

    var titleError by remember { mutableStateOf<String?>(null) } // made for errorHandling
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        OutlinedTextField( // a textinput field for title
            value = title,
            onValueChange = { title = it },
            label = { Text("Rubrik") },
            isError = titleError != null,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        titleError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        OutlinedTextField( // input field for desc
            value = description,
            onValueChange = { description = it },
            label = { Text("Beskrivning") },
            isError = descriptionError != null,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        descriptionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        OutlinedTextField( // input field for date
            value = date,
            onValueChange = { date = it },
            label = { Text("Datum") },
            isError = dateError != null,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        dateError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button( // the btn that changes shape depandant on uppdate or create
            onClick = {

                titleError = if (title.isBlank()) { // some error handling for title
                    "Titeln kan inte vara tom"
                }
                else if (title.length > 50){
                    "Titeln är för lång"
                }
                else if (title.length < 3) {
                    "Titeln måste vara minst 3 tecken"
                }
                else if (title[0] == title[1] && title[1] == title[2] && title[2] == title[3]) {
                    "Det där är inget ord... försök igen"
                }
                else if (!title[0].isUpperCase()) {
                    "Titeln ska börja med stor bokstav för bättre läsbarhet"
                }
                else {
                    null
                }

                descriptionError = if (description.length > 120) "Beskrivningen får inte vara mer än 120 tecken" else null // error handling for desc
                dateError = if (date.isBlank()) "Datum måste anges för bättre struktur" else null // error handling for date

                //if no errors , create / uppdare the note , with the current notelist size +1 as id , inizilize title,desc,date
                if (titleError == null && descriptionError == null && dateError == null) {
                    val newNote = Note(
                        id = noteId ?: (noteList.size + 1),
                        title = title,
                        description = description,
                        date = date
                    )
                    if (isUpdating && note != null) { // if isuppdating is true and the not trys to update a note that not exist
                        noteList[noteList.indexOf(note)] = newNote // give the new or uppdates note
                    } else {
                        noteList.add(newNote) // else add new to the stack
                    }
                    navController.popBackStack() // when we made this we want to come back to start
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B7355)) // hex color
        ) {
            Text(if (isUpdating) "UPPDATERA ANTECKNING" else "LÄGG TILL ANTECKNING") // if isupdating true change shape to updatera
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(navController: NavController, noteList: MutableList<Note>, noteId: Int) {
    val note = noteList.find { it.id == noteId } // Looking for the note that has right id
    Scaffold(
        topBar = { // topbar
            TopAppBar(
                title = { Text("Anteckning Detaljer") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Tillbaka") // i guess the arrowBack has a line over it becouse they recomend any other method, but it works
                    }
                }
            )
        }
    ) { perfectPadding ->
        Column(
            modifier = Modifier
                .padding(perfectPadding)
                .padding(15.dp) // spacing around
        ) {
            Text( // text over content
                text = "Rubrik:",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFD2B48C)
            )
            Text( // content
                text = note?.title ?: "",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 15.dp)
            )

            Text( // text over content
                text = "Beskrivning:",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFD2B48C)
            )
            Text( // content
                text = note?.description ?: "",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 15.dp)
            )

            Text(// text over content
                text = "Datum:",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFD2B48C)
            )
            Text(// content
                text = note?.date ?: "",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 15.dp)
            )

            Spacer(modifier = Modifier.height(14.dp)) // makes an space between the catogorys

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button( // IF u click this one we navigate to our route updateNote, and then the updateNote call mopdifNotescreen
                    onClick = {
                        navController.navigate("updateNote/$noteId")

                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B7355))
                ) {
                    Text("UPDATERA")
                }
                Button(
                    onClick = {
                        noteList.removeIf { it.id == noteId } // if the id right
                        navController.popBackStack() // pop it away from stack
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("RADERA")
                }
            }

        }
    }
}

