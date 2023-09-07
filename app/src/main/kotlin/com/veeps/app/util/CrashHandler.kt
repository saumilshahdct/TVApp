package com.veeps.app.util

import android.os.Build
import android.os.Process
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class CrashHandler : Thread.UncaughtExceptionHandler {
	override fun uncaughtException(thread: Thread, exception: Throwable) {
		val stackTrace = StringWriter()
		exception.printStackTrace(PrintWriter(stackTrace))
		val errorReport = """App Got Crashed
            
            ************ CAUSE OF ERROR ************
            
            $stackTrace
            
            ************ DEVICE INFORMATION ***********
            
            Brand: ${Build.BRAND}
            Device: ${Build.DEVICE}
            Model: ${Build.MODEL}
            Id: ${Build.ID}
            Product: ${Build.PRODUCT}

            ************ FIRMWARE ************

            SDK: ${Build.VERSION.SDK_INT}
            Release: ${Build.VERSION.RELEASE}
            Incremental: ${Build.VERSION.INCREMENTAL}
            """.trimIndent()
		Logger.printError(errorReport)
		Process.killProcess(Process.myPid())
		exitProcess(10)
	}
}