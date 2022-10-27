package com.example.sogating_app.setting

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sogating_app.MAIN.MainActivity
import com.example.sogating_app.Message.img.ImgApi
import com.example.sogating_app.Message.img.ResponseData
import com.example.sogating_app.R
import com.example.sogating_app.auth.UserDataModel
import com.example.sogating_app.utils.FirebaseAuthUtils
import com.example.sogating_app.utils.FirebaseRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_my_page.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class MyPageActivity : AppCompatActivity() {

    private val TAG = "MyPageActivity::class.java"
    private val uid = FirebaseAuthUtils.getUid()
    lateinit var myImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        //뒤로가기 버튼 누르면 메인액티비티로 감
        var back_button = findViewById<ImageView>(R.id.back_button_img)
        back_button.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        getMyData()

        /* 이미지 클릭해서 교체하기 */
        myImage = findViewById(R.id.myImage)
        // 이미지를 클릭하면 핸드폰에 저장되어있는 이미지들을 불러옴.
        // registerForActivityResult()를 통해서 핸드폰에 저장되어있는 저장소에 접근한다.
        val getAction = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                myImage.setImageURI(uri)
                scanImg()


            }
        )
        // getAction.launch() 메소드를 통해서 저장된 이미지를 변경.
        myImage.setOnClickListener {
            getAction.launch("image/*")

        }

        // 변경버튼
        changebtn.setOnClickListener{
            changeUserData(uid)
            uploadImage(uid)
            Toast.makeText(this, "변경이 완료 되었습니다", Toast.LENGTH_SHORT).show()
        }

        // 로그아웃버튼
        logoutbtn.setOnClickListener{
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
    }

    //유저 데이터 변경
    private fun changeUserData(uid: String) {

    }

    // 이미지 재저장
    private fun uploadImage(uid: String) {
        // Firebase 저장되는 경로 지정.
        val storage = Firebase.storage
        val storageRef = storage.reference.child(uid + ".png")

        // Get the data from an ImageView as bytes
        myImage.isDrawingCacheEnabled = true
        myImage.buildDrawingCache()
        val bitmap = (myImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = storageRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }

    //유저의 사진을 받아  flask_얼굴 인신 서버에 전송하여 얼굴 사진의 여부 파악
    private fun scanImg(){
        //img를 byteArray로 변환
        myImage.isDrawingCacheEnabled = true
        myImage.buildDrawingCache()
        val bitmap = (myImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()


        //retrofit를 이용하여 flask_server로 byteArray를 전송
        val api = ImgApi.create();
        api.getScanResult(data.toString()).enqueue(object : Callback<ResponseData>{
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                Log.d("ResponseData: ",response.body().toString())
                Toast.makeText(getApplicationContext(), "얼굴인식 완료",Toast.LENGTH_SHORT).show();

            }

            override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                Log.d("log","fail")
                Toast.makeText(getApplicationContext(), "얼굴인식 실패 다른 사진을 선택해 주세요",Toast.LENGTH_SHORT).show();

            }

        })

    }



    // Firebase에서 회원의 정보를 가져오기.
    private fun getMyData() {
        val myImage = findViewById<ImageView>(R.id.myImage)
        val myUid = findViewById<TextView>(R.id.myUID)
        val myNickname = findViewById<TextView>(R.id.myNickname)
        val myAge = findViewById<TextView>(R.id.myAge)
        val myCity = findViewById<TextView>(R.id.myCity)
        val myGender = findViewById<TextView>(R.id.myGender)


        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                Log.w(TAG, dataSnapshot.toString())
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                myUid.text = data!!.uid

                myNickname.text = data!!.nickname
                myAge.text = data!!.age
                myCity.text = data!!.city
                myGender.text = data!!.gender

                // Firebase에 저장된 이미지를 가져온다.
                val storageRef = Firebase.storage.reference.child(data.uid + ".png")
                storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Glide.with(baseContext)
                            .load(task.result)
                            .into(myImage)
                    }
                })

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // 데이터가 어디에 정의되어 있는 냐?
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }
}