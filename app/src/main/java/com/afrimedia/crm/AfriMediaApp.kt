package com.afrimedia.crm

import android.app.Application
import com.afrimedia.crm.data.remote.SessionManager
import com.afrimedia.crm.data.repo.Repository

class AfriMediaApp : Application() {
    lateinit var session: SessionManager
        private set
    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        session = SessionManager(this)
        repository = Repository(session)
    }
}
