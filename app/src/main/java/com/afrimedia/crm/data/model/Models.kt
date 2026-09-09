package com.afrimedia.crm.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Currency(
    val code: String = "",
    val symbol: String = ""
)

@Serializable
data class Me(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    @SerialName("is_admin") val isAdmin: Boolean = false,
    @SerialName("branch_id") val branchId: Int? = null,
    @SerialName("branch_name") val branchName: String? = null,
    @SerialName("base_currency") val baseCurrency: String = "USD",
    val currencies: List<Currency> = emptyList(),
    @SerialName("tax_label") val taxLabel: String = "VAT"
)

@Serializable
data class Branch(
    val id: Int = 0,
    val name: String = "",
    @SerialName("is_head_office") val isHeadOffice: Boolean = false
)

@Serializable
data class BranchSummary(
    @SerialName("branch_id") val branchId: Int = 0,
    @SerialName("branch_name") val branchName: String = "",
    val invoiced: Double = 0.0,
    val collected: Double = 0.0,
    val outstanding: Double = 0.0
)

@Serializable
data class Dashboard(
    @SerialName("date_from") val dateFrom: String = "",
    @SerialName("date_to") val dateTo: String = "",
    @SerialName("base_currency") val baseCurrency: String = "USD",
    val invoiced: Double = 0.0,
    val collected: Double = 0.0,
    val outstanding: Double = 0.0,
    val branches: List<BranchSummary> = emptyList()
)

@Serializable
data class Client(
    val id: Int = 0,
    @SerialName("branch_id") val branchId: Int? = null,
    @SerialName("client_type") val clientType: String = "individual",
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name") val lastName: String = "",
    val company: String = "",
    @SerialName("display_name") val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    @SerialName("contact_person") val contactPerson: String = "",
    @SerialName("contact_phone") val contactPhone: String = "",
    @SerialName("contact_email") val contactEmail: String = "",
    @SerialName("address_line1") val addressLine1: String = "",
    @SerialName("address_line2") val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    @SerialName("postal_code") val postalCode: String = "",
    val country: String = "",
    @SerialName("tax_number") val taxNumber: String = "",
    @SerialName("preferred_currency") val preferredCurrency: String = "USD",
    val notes: String = "",
    val status: String = "active"
)

@Serializable
data class LineItem(
    val description: String = "",
    val qty: Double = 1.0,
    @SerialName("unit_price") val unitPrice: Double = 0.0,
    @SerialName("tax_rate") val taxRate: Double = 0.0,
    @SerialName("line_total") val lineTotal: Double = 0.0
)

@Serializable
data class Payment(
    val id: Int = 0,
    val amount: Double = 0.0,
    val currency: String = "",
    val method: String = "",
    val reference: String = "",
    @SerialName("paid_on") val paidOn: String = "",
    val notes: String = ""
)

/** Shared shape for both quotes and invoices — the REST API returns the same envelope for both. */
@Serializable
data class Document(
    val id: Int = 0,
    val number: String = "",
    @SerialName("client_id") val clientId: Int = 0,
    @SerialName("client_name") val clientName: String = "",
    @SerialName("branch_id") val branchId: Int? = null,
    @SerialName("branch_name") val branchName: String? = null,
    val currency: String = "USD",
    @SerialName("exchange_rate") val exchangeRate: Double = 1.0,
    val subtotal: Double = 0.0,
    @SerialName("discount_type") val discountType: String = "fixed",
    @SerialName("discount_value") val discountValue: Double = 0.0,
    @SerialName("discount_total") val discountTotal: Double = 0.0,
    @SerialName("tax_rate") val taxRate: Double = 0.0,
    @SerialName("tax_total") val taxTotal: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "draft",
    @SerialName("issue_date") val issueDate: String = "",
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("expiry_date") val expiryDate: String? = null,
    val notes: String = "",
    val terms: String = "",
    val items: List<LineItem> = emptyList(),
    @SerialName("amount_paid") val amountPaid: Double? = null,
    @SerialName("balance_due") val balanceDue: Double? = null,
    val payments: List<Payment> = emptyList(),
    val client: Client? = null
)

data class ApiFailure(val message: String, val httpCode: Int? = null)

/* ---------------------------------------------------------------- */
/* Projects / Tasks — mirrors project_payload()/task_payload()/etc.  */
/* in includes/class-amcrm-rest-api.php. task_lists/profitability/   */
/* linked_documents are only present on the single-project GET       */
/* (include_task_lists = true); subtasks/comments/time_entries are   */
/* only present on the single-task GET.                              */
/* ---------------------------------------------------------------- */

@Serializable
data class Profitability(
    @SerialName("revenue_invoiced") val revenueInvoiced: Double = 0.0,
    @SerialName("revenue_collected") val revenueCollected: Double = 0.0,
    @SerialName("revenue_quoted") val revenueQuoted: Double = 0.0,
    @SerialName("hours_logged") val hoursLogged: Double = 0.0,
    @SerialName("has_rate") val hasRate: Boolean = false,
    @SerialName("hourly_rate") val hourlyRate: Double? = null,
    val cost: Double? = null,
    val profit: Double? = null,
    @SerialName("base_currency") val baseCurrency: String = "USD"
)

@Serializable
data class LinkedDocuments(
    val quotes: List<Document> = emptyList(),
    val invoices: List<Document> = emptyList()
)

@Serializable
data class TimeEntry(
    val id: Int = 0,
    @SerialName("task_id") val taskId: Int = 0,
    @SerialName("project_id") val projectId: Int? = null,
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("user_name") val userName: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("ended_at") val endedAt: String? = null,
    @SerialName("duration_minutes") val durationMinutes: Int = 0,
    @SerialName("is_running") val isRunning: Boolean = false,
    val note: String = ""
)

@Serializable
data class TaskComment(
    val id: Int = 0,
    @SerialName("task_id") val taskId: Int = 0,
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("user_name") val userName: String? = null,
    @SerialName("comment_type") val commentType: String = "comment",
    val body: String = "",
    val meta: JsonElement? = null,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class Task(
    val id: Int = 0,
    @SerialName("project_id") val projectId: Int = 0,
    @SerialName("task_list_id") val taskListId: Int = 0,
    @SerialName("branch_id") val branchId: Int? = null,
    @SerialName("parent_task_id") val parentTaskId: Int? = null,
    val title: String = "",
    val description: String = "",
    val status: String = "to_do",
    val priority: String = "normal",
    @SerialName("assignee_id") val assigneeId: Int? = null,
    @SerialName("assignee_name") val assigneeName: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("total_minutes") val totalMinutes: Int = 0,
    @SerialName("created_at") val createdAt: String = "",
    val subtasks: List<Task> = emptyList(),
    val comments: List<TaskComment> = emptyList(),
    @SerialName("time_entries") val timeEntries: List<TimeEntry> = emptyList()
)

@Serializable
data class TaskList(
    val id: Int = 0,
    @SerialName("project_id") val projectId: Int = 0,
    @SerialName("branch_id") val branchId: Int? = null,
    val name: String = "",
    @SerialName("sort_order") val sortOrder: Int = 0,
    val tasks: List<Task> = emptyList()
)

@Serializable
data class Project(
    val id: Int = 0,
    @SerialName("client_id") val clientId: Int = 0,
    @SerialName("branch_id") val branchId: Int? = null,
    val name: String = "",
    val description: String = "",
    val statuses: List<String> = emptyList(),
    @SerialName("hourly_rate") val hourlyRate: Double? = null,
    val status: String = "active",
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("task_lists") val taskLists: List<TaskList> = emptyList(),
    val profitability: Profitability? = null,
    @SerialName("linked_documents") val linkedDocuments: LinkedDocuments? = null
)

/* ---------------------------------------------------------------- */
/* Write request bodies — kept separate from the read models above   */
/* since kotlinx.serialization needs a concrete, fully-typed shape   */
/* for the JSON body (a generic Map<String, Any?> can't be           */
/* serialized automatically).                                        */
/* ---------------------------------------------------------------- */

@Serializable
data class ClientRequest(
    @SerialName("branch_id") val branchId: Int? = null,
    @SerialName("client_type") val clientType: String = "individual",
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name") val lastName: String = "",
    val company: String = "",
    val email: String = "",
    val phone: String = "",
    @SerialName("contact_person") val contactPerson: String = "",
    @SerialName("contact_phone") val contactPhone: String = "",
    @SerialName("contact_email") val contactEmail: String = "",
    @SerialName("address_line1") val addressLine1: String = "",
    @SerialName("address_line2") val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    @SerialName("postal_code") val postalCode: String = "",
    val country: String = "",
    @SerialName("tax_number") val taxNumber: String = "",
    @SerialName("preferred_currency") val preferredCurrency: String = "USD",
    val notes: String = "",
    val status: String = "active"
)

@Serializable
data class LineItemRequest(
    val description: String,
    val qty: Double,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("tax_rate") val taxRate: Double = 0.0
)

@Serializable
data class DocumentRequest(
    @SerialName("client_id") val clientId: Int,
    @SerialName("branch_id") val branchId: Int? = null,
    val currency: String,
    @SerialName("discount_type") val discountType: String = "fixed",
    @SerialName("discount_value") val discountValue: Double = 0.0,
    @SerialName("tax_rate") val taxRate: Double = 0.0,
    @SerialName("issue_date") val issueDate: String,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("expiry_date") val expiryDate: String? = null,
    val notes: String = "",
    val terms: String = "",
    val items: List<LineItemRequest> = emptyList()
)

@Serializable
data class PaymentRequest(
    val amount: Double,
    val currency: String,
    val method: String = "",
    val reference: String = "",
    @SerialName("paid_on") val paidOn: String,
    val notes: String = ""
)

@Serializable
data class ProjectRequest(
    @SerialName("client_id") val clientId: Int,
    @SerialName("branch_id") val branchId: Int? = null,
    val name: String,
    val description: String = "",
    /** Custom Kanban column labels; null/empty lets the backend fall back to its defaults. */
    val statuses: List<String>? = null,
    @SerialName("hourly_rate") val hourlyRate: Double? = null,
    val status: String = "active",
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("due_date") val dueDate: String? = null
)

@Serializable
data class TaskListRequest(
    val name: String
)

/** Body for POST /tasks (create) — project_id/task_list_id/title are required by the backend. */
@Serializable
data class TaskRequest(
    @SerialName("project_id") val projectId: Int,
    @SerialName("task_list_id") val taskListId: Int,
    @SerialName("parent_task_id") val parentTaskId: Int? = null,
    val title: String,
    val description: String = "",
    val status: String = "to_do",
    val priority: String = "normal",
    @SerialName("assignee_id") val assigneeId: Int? = null,
    @SerialName("due_date") val dueDate: String? = null
)

/**
 * Body for PUT/PATCH /tasks/{id} — every field is nullable/omitted-when-null
 * (see ApiClient's explicitNulls = false) so only the fields being changed
 * are sent, matching update_task()'s per-field null check server-side.
 */
@Serializable
data class TaskUpdateRequest(
    @SerialName("task_list_id") val taskListId: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val status: String? = null,
    val priority: String? = null,
    @SerialName("assignee_id") val assigneeId: Int? = null,
    @SerialName("due_date") val dueDate: String? = null
)

@Serializable
data class StatusChangeRequest(
    val status: String
)

@Serializable
data class CommentRequest(
    val body: String
)

@Serializable
data class TimeEntryRequest(
    @SerialName("duration_minutes") val durationMinutes: Int,
    val note: String = "",
    val date: String? = null
)
