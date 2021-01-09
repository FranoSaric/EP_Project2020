package com.example.scanner

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*


interface APIService {


    /*
       POST METHOD
    */

    // Raw JSON
    @POST("/api/v1/create")
    suspend fun createEmployee(@Body requestBody: RequestBody): Response<ResponseBody>


    // Form Data
    @Multipart
    @POST("/post")
    suspend fun uploadEmployeeData(@PartMap map: HashMap<String?, RequestBody?>): Response<ResponseBody>


    // Encoded URL
    @FormUrlEncoded
    @POST("/post")
    suspend fun createEmployee(@FieldMap params: HashMap<String?, String?>): Response<ResponseBody>


    /*****************************************************************************************************************************************************/



}