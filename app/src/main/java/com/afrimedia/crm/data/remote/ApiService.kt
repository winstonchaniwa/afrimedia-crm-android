package com.afrimedia.crm.data.remote

import com.afrimedia.crm.data.model.Branch
import com.afrimedia.crm.data.model.Client
import com.afrimedia.crm.data.model.ClientRequest
import com.afrimedia.crm.data.model.CommentRequest
import com.afrimedia.crm.data.model.Dashboard
import com.afrimedia.crm.data.model.Document
import com.afrimedia.crm.data.model.DocumentRequest
import com.afrimedia.crm.data.model.Me
import com.afrimedia.crm.data.model.PaymentRequest
import com.afrimedia.crm.data.model.Project
import com.afrimedia.crm.data.model.ProjectRequest
import com.afrimedia.crm.data.model.StatusChangeRequest
import com.afrimedia.crm.data.model.Task
import com.afrimedia.crm.data.model.TaskComment
import com.afrimedia.crm.data.model.TaskList
import com.afrimedia.crm.data.model.TaskListRequest
import com.afrimedia.crm.data.model.TaskRequest
import com.afrimedia.crm.data.model.TaskUpdateRequest
import com.afrimedia.crm.data.model.TimeEntry
import com.afrimedia.crm.data.model.TimeEntryRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Mirrors the routes registered in the plugin's
 * includes/class-amcrm-rest-api.php (namespace amcrm/v1). All requests
 * carry HTTP Basic Auth (site username + WordPress Application Password)
 * via BasicAuthInterceptor — see ApiClient.
 */
interface ApiService {

    @GET("me")
    suspend fun getMe(): Response<Me>

    @GET("branches")
    suspend fun getBranches(): Response<List<Branch>>

    @GET("dashboard")
    suspend fun getDashboard(
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("branch_id") branchId: Int? = null
    ): Response<Dashboard>

    // Clients
    @GET("clients")
    suspend fun listClients(
        @Query("search") search: String? = null,
        @Query("branch_id") branchId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): Response<List<Client>>

    @GET("clients/{id}")
    suspend fun getClient(@Path("id") id: Int): Response<Client>

    @POST("clients")
    suspend fun createClient(@Body body: ClientRequest): Response<Client>

    @POST("clients/{id}")
    suspend fun updateClient(@Path("id") id: Int, @Body body: ClientRequest): Response<Client>

    @DELETE("clients/{id}")
    suspend fun deleteClient(@Path("id") id: Int): Response<Unit>

    // Quotes
    @GET("quotes")
    suspend fun listQuotes(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("branch_id") branchId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): Response<List<Document>>

    @GET("quotes/{id}")
    suspend fun getQuote(@Path("id") id: Int): Response<Document>

    @POST("quotes")
    suspend fun createQuote(@Body body: DocumentRequest): Response<Document>

    @POST("quotes/{id}")
    suspend fun updateQuote(@Path("id") id: Int, @Body body: DocumentRequest): Response<Document>

    @DELETE("quotes/{id}")
    suspend fun deleteQuote(@Path("id") id: Int): Response<Unit>

    @POST("quotes/{id}/convert")
    suspend fun convertQuote(@Path("id") id: Int): Response<Document>

    // Invoices
    @GET("invoices")
    suspend fun listInvoices(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("branch_id") branchId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): Response<List<Document>>

    @GET("invoices/{id}")
    suspend fun getInvoice(@Path("id") id: Int): Response<Document>

    @POST("invoices")
    suspend fun createInvoice(@Body body: DocumentRequest): Response<Document>

    @POST("invoices/{id}")
    suspend fun updateInvoice(@Path("id") id: Int, @Body body: DocumentRequest): Response<Document>

    @DELETE("invoices/{id}")
    suspend fun deleteInvoice(@Path("id") id: Int): Response<Unit>

    @POST("invoices/{id}/payments")
    suspend fun recordPayment(@Path("id") id: Int, @Body body: PaymentRequest): Response<Document>

    // Projects
    @GET("projects")
    suspend fun listProjects(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("client_id") clientId: Int? = null,
        @Query("branch_id") branchId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): Response<List<Project>>

    @GET("projects/{id}")
    suspend fun getProject(@Path("id") id: Int): Response<Project>

    @POST("projects")
    suspend fun createProject(@Body body: ProjectRequest): Response<Project>

    @POST("projects/{id}")
    suspend fun updateProject(@Path("id") id: Int, @Body body: ProjectRequest): Response<Project>

    @DELETE("projects/{id}")
    suspend fun deleteProject(@Path("id") id: Int): Response<Unit>

    // Task lists
    @GET("projects/{id}/task-lists")
    suspend fun listTaskLists(@Path("id") projectId: Int): Response<List<TaskList>>

    @POST("projects/{id}/task-lists")
    suspend fun createTaskList(@Path("id") projectId: Int, @Body body: TaskListRequest): Response<TaskList>

    // Tasks
    @POST("tasks")
    suspend fun createTask(@Body body: TaskRequest): Response<Task>

    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") id: Int): Response<Task>

    @POST("tasks/{id}")
    suspend fun updateTask(@Path("id") id: Int, @Body body: TaskUpdateRequest): Response<Task>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<Unit>

    @POST("tasks/{id}/status")
    suspend fun setTaskStatus(@Path("id") id: Int, @Body body: StatusChangeRequest): Response<Task>

    // Task comments
    @GET("tasks/{id}/comments")
    suspend fun listTaskComments(@Path("id") taskId: Int): Response<List<TaskComment>>

    @POST("tasks/{id}/comments")
    suspend fun addTaskComment(@Path("id") taskId: Int, @Body body: CommentRequest): Response<TaskComment>

    // Time tracking
    @POST("tasks/{id}/timer/start")
    suspend fun startTaskTimer(@Path("id") taskId: Int): Response<TimeEntry>

    @POST("tasks/{id}/timer/stop")
    suspend fun stopTaskTimer(@Path("id") taskId: Int): Response<TimeEntry>

    @POST("tasks/{id}/time-entries")
    suspend fun addTaskTimeEntry(@Path("id") taskId: Int, @Body body: TimeEntryRequest): Response<TimeEntry>
}
