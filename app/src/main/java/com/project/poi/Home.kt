package com.project.poi

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.fcfm.poi.plantilla.models.Grupo
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.project.poi.adaptadores.GroupsAdapter
import kotlinx.android.synthetic.main.activity_chat_room.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_usuarios.*
import kotlinx.android.synthetic.main.fragment_perfil.*


class Home : AppCompatActivity() {

    private val dataBase = FirebaseDatabase.getInstance()
    private val dbRef = dataBase.getReference("Grupos")
    val listGroups = mutableListOf<Grupo>()
    private val adaptador = GroupsAdapter(this, listGroups)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)



        //val activity_chat = Intent(this,ChatRoom::class.java)
        //val vista_chat= findViewById<NavigationView>(R.id.menu_navbarb)

      //val itemlist = (ListView)findViewById(R.id.menu_chat)

       // itemlist.setOnClickListener{
         //   val intent =Intent(this,ChatRoom::class.java)
        //    startActivity(intent)
       // }
        val drawer = findViewById<DrawerLayout>(R.id.view2)

        val toolbar:androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar);
        val navbutton =findViewById<BottomNavigationView>(R.id.menu_navbarb)
        val navC = findNavController(R.id.fragment)
        navbutton.setupWithNavController(navC)

       // Drawable drawable = ContextCompat.getDrawable(this,R.id.drawer)
        //val drawable: Drawable? = ContextCompat.getDrawable(this,R.id.drawer)
      val toggle :ActionBarDrawerToggle = ActionBarDrawerToggle(this,drawer,toolbar,
      R.string.nav_app_bar_open_drawer_description,R.string.nav_app_bar_navigate_up_description)
        drawer.addDrawerListener(toggle)
        toggle.getDrawerArrowDrawable().setColor(getColor(R.color.white));
        toggle.syncState()

        val sharedPreferences : SharedPreferences = getSharedPreferences(
            "SharedP",
            Context.MODE_PRIVATE
        )

        val currentUser = sharedPreferences.getString("key", "")
        val userName = sharedPreferences.getString("userName", "")
        txt_nameToolbar.text = userName.toString()

        val groupName = intent.extras?.getString("groupName")
        val groupId = intent.extras?.getString("groupId")

        if(groupName != null){
            textocentrado.text = groupName.toString()
        }

         if(groupId != null){
             val frag = buscar()
             val args = Bundle()
             args.putString("groupId", groupId)
             args.putString("key", currentUser)
             frag.setArguments(args)
             val transaction = supportFragmentManager.beginTransaction()
             transaction.replace(R.id.fragment, frag)
             transaction.commit()
             //Tranlate fragment
                btnAddUser.setOnClickListener {
                val activity = Intent(this, MuestraU::class.java)
                activity.putExtra("groupId", groupId)
                startActivity(activity)
          }

             btn_add_homework.setOnClickListener {
                 val activity = Intent(this, create_homework::class.java)
                 activity.putExtra("groupId", groupId)
                 startActivity(activity)
             }
        }







        btnAddNewGroup.setOnClickListener{
            val activityAdd = Intent(this, AddGroupActivity::class.java)

            startActivity(activityAdd)
        }

        rvShowGroups.adapter = adaptador
        getGroups(currentUser.toString())


/*
        btnTestChat.setOnClickListener{
            val intent = Intent(this, ChatRoom::class.java)
            intent.putExtra("chatClase", "grupo")
            startActivity(intent)
        }
*/
    }

            fun getGroups(currentUser: String){
                dbRef.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        listGroups.clear()
                        var key = ""
                        var carreraG = ""
                        for (snap in snapshot.children) {
                            var childKey = snap.key.toString()
                            carreraG = snap.child("carrera").value.toString()
                            var Personas = snap.child("Integrantes").children
                            for (persona in Personas) {
                                key = persona.child("key").value.toString()
                                var nombrePersona = persona.child("nombre").value.toString()
                                var carreraU = persona.child("carrera").value.toString()
                                if (key == currentUser && carreraG == carreraU) {
                                    var carrera = snap.child("carrera").value.toString();
                                    var id = snap.child("id").value.toString();
                                    var nombre = snap.child("nombre").value.toString();

                                    listGroups.add(Grupo(id, carrera, nombre))

                                }
                            }


                        }
                        if (listGroups.size > 0) {
                            adaptador.notifyDataSetChanged()

                        }
                    }
                })
                adaptador.notifyDataSetChanged()
            }


    //menu_chat.setOnClickListener { startActivity(activity_chat) }


    override fun onPause() {
        super.onPause()

        val sharedPreferences : SharedPreferences = getSharedPreferences(
            "SharedP",
            Context.MODE_PRIVATE
        )


        val key = sharedPreferences.getString("key", "")
           dataBase.getReference("users/" + key + "/status").setValue("false")
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences : SharedPreferences = getSharedPreferences("SharedP", Context.MODE_PRIVATE)
        var changeStatus: String? = sharedPreferences.getString("status","")





        val key = sharedPreferences.getString("key","")


        if(changeStatus == "true"){
            dataBase.getReference("users/" + key + "/status").setValue("true")
        }
       else{
            dataBase.getReference("users/" + key + "/status").setValue("false")
        }
    }

}