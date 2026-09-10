package com.example.movilesa20262

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val listaPosts = mutableStateOf<List<Post>>(emptyList())
    private var ultimoDocumento: DocumentSnapshot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargamos los primeros 5 posts al iniciar la app
        cargarNuevosPosts()

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

                    // Lista para mostrar los posts de Firebase
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(listaPosts.value) { post ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = post.texto, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón de paginación para cargar los siguientes 5 posts
                    Button(
                        onClick = { cargarMasPosts() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Cargar más posts")
                    }
                }
            }
        }
    }

    // 1. Cargar los primeros 5 posts más recientes
    private fun cargarNuevosPosts() {
        db.collection("posts")
            .orderBy("fecha", Query.Direction.DESCENDING) // Del más nuevo al más antiguo
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val nuevosPosts = mutableListOf<Post>()
                    for (document in documents) {
                        val post = document.toObject(Post::class.java)
                        nuevosPosts.add(post)
                    }
                    // Guardamos la referencia del último documento para la paginación
                    ultimoDocumento = documents.documents[documents.size() - 1]
                    listaPosts.value = nuevosPosts
                }
            }
    }

    // 2. Cargar los siguientes 5 posts al presionar el botón
    private fun cargarMasPosts() {
        val ultimo = ultimoDocumento ?: return // Si es null, no hace nada

        db.collection("posts")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .startAfter(ultimo) // Empieza a buscar después del último documento cargado
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val postsActuales = listaPosts.value.toMutableList()
                    for (document in documents) {
                        val post = document.toObject(Post::class.java)
                        postsActuales.add(post)
                    }
                    // Actualizamos el nuevo último documento
                    ultimoDocumento = documents.documents[documents.size() - 1]
                    listaPosts.value = postsActuales
                }
            }
    }
}