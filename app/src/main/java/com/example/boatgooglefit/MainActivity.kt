package com.example.boatgooglefit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.boatgooglefit.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.SessionReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.android.gms.fitness.result.SessionReadResponse
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import java.text.DateFormat
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private lateinit var binding: ActivityMainBinding

    var startDate: Long = 0
    var endDate: Long = 0
    private val dateFormat = DateFormat.getDateInstance()
    private val fitnessOptions: FitnessOptions by lazy {
        FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
            .addDataType(
                HealthDataTypes.AGGREGATE_BLOOD_PRESSURE_SUMMARY,
                FitnessOptions.ACCESS_READ
            )
            .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_READ)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val cal = Calendar.getInstance()
        val now = Date()
        cal.time = now
        endDate = cal.timeInMillis
        cal.add(Calendar.WEEK_OF_YEAR, -1)
        startDate = cal.timeInMillis
        binding.subView.textDateRange.text =
            convertLongToTime(startDate) + " - " + convertLongToTime(endDate)
        fitSignIn()

        binding.subView.buttonFirst.setOnClickListener {
            GoogleSignIn.requestPermissions(
                this,
                FitActionRequestCode.SUBSCRIBE.ordinal,
                getGoogleAccount(), fitnessOptions
            )
        }
        binding.subView.textSelectedDateRange.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.dateRangePicker().build()
            datePicker.show(supportFragmentManager, "DatePicker")

            // Setting up the event for when ok is clicked
            datePicker.addOnPositiveButtonClickListener {
                startDate = datePicker.selection!!.first
                endDate = datePicker.selection!!.second
                binding.subView.textDateRange.text =
                    convertLongToTime(startDate) + " - " + convertLongToTime(endDate)
                fitSignIn()
            }

            // Setting up the event for when cancelled is clicked
            datePicker.addOnNegativeButtonClickListener {
            }

            // Setting up the event for when back button is pressed
            datePicker.addOnCancelListener {
                Toast.makeText(this, "Date Picker Cancelled", Toast.LENGTH_LONG).show()
            }
        }

        checkPermissionsAndRun(FitActionRequestCode.READ_DATA)

        binding.subView.buttonHeadTracking.setOnClickListener {
            showBottomDialog()
        }
    }

    private fun checkPermissionsAndRun(fitActionRequestCode: FitActionRequestCode) {
        if (permissionApproved()) {
            fitSignIn()
        } else {
            requestRuntimePermissions(fitActionRequestCode)
        }
    }

    private fun permissionApproved(): Boolean {
        val approved = if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        } else {
            true
        }
        return approved
    }

    private fun requestRuntimePermissions(requestCode: FitActionRequestCode) {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        requestCode.let {
            if (shouldProvideRationale) {
                Log.i(TAG, "Displaying permission rationale to provide additional context.")
                Snackbar.make(
                    findViewById(R.id.sub_view),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.ok) {
                        // Request permission
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                            requestCode.ordinal
                        )
                    }
                    .show()
            } else {
                Log.i(TAG, "Requesting permission")
                // Request permission. It's possible this can be auto answered if device policy
                // sets the permission in a given state or the user denied the permission
                // previously and checked "Never ask again".
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    requestCode.ordinal
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when {
            grantResults.isEmpty() -> {
                // If user interaction was interrupted, the permission request
                // is cancelled and you receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            }
            grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                // Permission was granted.
                val fitActionRequestCode = FitActionRequestCode.values()[requestCode]
                fitActionRequestCode.let {
                    fitSignIn()
                }
            }
            else -> {
                // Permission denied.

                // In this Activity we've chosen to notify the user that they
                // have rejected a core permission for the app since it makes the Activity useless.
                // We're communicating this message in a Snackbar since this is a sample app, but
                // core permissions would typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.

                Snackbar.make(
                    findViewById(R.id.sub_view),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .show()
            }
        }
    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd MMM yyyy")
        return format.format(date)
    }

    private fun fitSignIn() {
        if (oAuthPermissionsApproved()) {
            binding.subView.buttonFirst.visibility = View.GONE
            binding.subView.parentLayout.visibility = View.VISIBLE
            Log.e(TAG, "Success")
            val dateFormat: DateFormat = getDateInstance()
            Log.e(TAG, "Range Start: " + dateFormat.format(startDate))
            Log.e(TAG, "Range End: " + dateFormat.format(endDate))
            readData(startDate, endDate)
            readBPM(startDate, endDate)
            readSleepSessions(startDate, endDate)
            readBloodPresser(startDate, endDate)
            readWeight(startDate, endDate)
        } else {
            binding.subView.parentLayout.visibility = View.GONE
            binding.subView.buttonFirst.visibility = View.VISIBLE
        }
    }

    private fun oAuthPermissionsApproved() =
        GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)

    private fun getGoogleAccount() = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            RESULT_OK -> {
                fitSignIn()
            }
            else -> oAuthErrorMsg(requestCode, resultCode)
        }
    }

    private fun oAuthErrorMsg(requestCode: Int, resultCode: Int) {
        val message = """
            There was an error signing into Fit. Check the troubleshooting section of the README
            for potential issues.
            Request code was: $requestCode
            Result code was: $resultCode
        """.trimIndent()
        Log.e(TAG, message)
    }


    private fun readData(startDate: Long, endDate: Long) {
        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_STEP_COUNT_DELTA)
            .enableServerQueries()
            .setTimeRange(startDate, endDate, TimeUnit.MILLISECONDS)
            .build()
        Fitness.getHistoryClient(this, getGoogleAccount())
            .readData(readRequest)
            .addOnSuccessListener { dataSet ->
                Log.i(TAG, "Total steps: $dataSet")
                val total = dataSet.getDataSet(DataType.TYPE_STEP_COUNT_DELTA)
                var totalSteps = 0
                for (dp in total.dataPoints) {
                    Log.e(TAG, "Data point:")
                    Log.e(TAG, "\tType: ${dp.dataType.name}")
                    dp.dataType.fields.forEach {
                        Log.e(TAG, "\tField: ${it.name} Value: ${dp.getValue(it)}")
                        totalSteps += dp.getValue(it).asInt()
                    }
                }
                binding.subView.textStepsCount.text = "$totalSteps"
                Log.i(TAG, "Total steps: $totalSteps")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }
    }

    private fun readBPM(startTime: Long, endTime: Long) {
        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_HEART_RATE_BPM)
            .enableServerQueries()
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()
        Fitness.getHistoryClient(this, getGoogleAccount())
            .readData(readRequest)
            .addOnSuccessListener { dataSet ->
                Log.i(TAG, "Total steps: $dataSet")
                val total = dataSet.getDataSet(DataType.TYPE_HEART_RATE_BPM)
                if (total.dataPoints.isEmpty()) {
                    binding.subView.heartRateCard.visibility = View.VISIBLE
                    binding.subView.textHartRate.text =
                        "Heart Rate : \n--"
                } else {
                    binding.subView.textHartRate.text =
                        "Heart Rate : \n${total.dataPoints.first().getValue(Field.FIELD_BPM)}"
                    binding.subView.heartRateCard.visibility = View.VISIBLE
                    Log.i(TAG, "Total steps: $total")
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }
    }

    //Step count
    private fun printData(dataReadResult: DataReadResponse) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.buckets.isNotEmpty()) {
            Log.i(TAG, "Number of returned buckets of DataSets is: " + dataReadResult.buckets.size)
            for (bucket in dataReadResult.buckets) {
                bucket.dataSets.forEach { dumpDataSet(it) }
            }
        } else if (dataReadResult.dataSets.isNotEmpty()) {
            Log.i(TAG, "Number of returned DataSets is: " + dataReadResult.dataSets.size)
            dataReadResult.dataSets.forEach { dumpDataSet(it) }
        }
        // [END parse_read_data_result]
    }

    private fun dumpDataSet(dataSet: DataSet) {
        Log.i(TAG, "Data returned for Data type: ${dataSet.dataType.name}")
        var totalSteps = 0
        for (dp in dataSet.dataPoints) {
            Log.e(TAG, "Data point:")
            Log.e(TAG, "\tType: ${dp.dataType.name}")
            dp.dataType.fields.forEach {
                Log.e(TAG, "\tField: ${it.name} Value: ${dp.getValue(it)}")
                totalSteps += dp.getValue(it).asInt()
            }
        }
        Log.e(TAG, "Total steps: $totalSteps")
    }



    private fun queryFitnessData(): DataReadRequest {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val now = Date()
        calendar.time = now
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
        val startTime = calendar.timeInMillis

        Log.i(TAG, "Range Start: ${dateFormat.format(startTime)}")
        Log.i(TAG, "Range End: ${dateFormat.format(endTime)}")

        return DataReadRequest.Builder()
            // The data request can specify multiple data types to return, effectively
            // combining multiple data queries into one call.
            // In this example, it's very unlikely that the request is for several hundred
            // datapoints each consisting of a few steps and a timestamp.  The more likely
            // scenario is wanting to see how many steps were walked per day, for 7 days.
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            // Analogous to a "Group By" in SQL, defines how data should be aggregated.
            // bucketByTime allows for a time span, whereas bucketBySession would allow
            // bucketing by "sessions", which would need to be defined in code.
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()
    }


    private fun readBloodPresser(startTime: Long, endTime: Long) {
        val readRequest = DataReadRequest.Builder()
            .read(HealthDataTypes.TYPE_BLOOD_PRESSURE)
            .read(HealthDataTypes.AGGREGATE_BLOOD_PRESSURE_SUMMARY)
            .enableServerQueries()
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()
        Fitness.getHistoryClient(this, getGoogleAccount())
            .readData(readRequest)
            .addOnSuccessListener { dataSet ->
                Log.i(TAG, "Total steps: $dataSet")
                val total = dataSet.getDataSet(HealthDataTypes.TYPE_BLOOD_PRESSURE)
                Log.i(TAG, "Total steps: $total")
//                printData(dataSet)
                if (total.dataPoints.isEmpty()) {
                    binding.subView.bloodPressureCard.visibility = View.VISIBLE
                    binding.subView.textBloodPressure.text = "Blood Pressure : \n-- "
                } else {
                    binding.subView.textBloodPressure.text = "Blood Pressure : \n${
                        total.dataPoints.first()
                            .getValue(HealthFields.FIELD_BLOOD_PRESSURE_SYSTOLIC)
                    } / ${
                        total.dataPoints.first()
                            .getValue(HealthFields.FIELD_BLOOD_PRESSURE_DIASTOLIC)
                    }"
                    binding.subView.bloodPressureCard.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }
    }


    private fun readWeight(startTime: Long, endTime: Long) {
        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_WEIGHT)
            .enableServerQueries()
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()
        Fitness.getHistoryClient(this, getGoogleAccount())
            .readData(readRequest)
            .addOnSuccessListener { dataSet ->
                Log.i(TAG, "Total steps: $dataSet")
                val total = dataSet.getDataSet(DataType.TYPE_WEIGHT)
                Log.i(TAG, "Total steps: $total")
//                printData(dataSet)
                if (total.dataPoints.isEmpty()) {
                    binding.subView.textWeight.text =
                        "Weight : \n--"
                } else {
                    binding.subView.textWeight.text =
                        "Weight : \n${total.dataPoints.first().getValue(Field.FIELD_WEIGHT)}"
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }
    }

    private fun readSleepSessions(startTime: Long, endTime: Long) {
        val client = Fitness.getSessionsClient(this, getGoogleAccount())

        val sessionReadRequest = SessionReadRequest.Builder()
            .read(DataType.TYPE_SLEEP_SEGMENT)
            // By default, only activity sessions are included, not sleep sessions. Specifying
            // includeSleepSessions also sets the behaviour to *exclude* activity sessions.
            .includeSleepSessions()
            .readSessionsFromAllApps()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        client.readSession(sessionReadRequest)
            .addOnSuccessListener { dumpSleepSessions(it) }
            .addOnFailureListener { Log.e(TAG, "Unable to read sleep sessions", it) }
    }


    private fun dumpSleepSessions(response: SessionReadResponse) {
//        Log.clear()
        if (response.sessions.isEmpty()) {
            binding.subView.textSleepDuration.text = "Sleep duration : \n--"
        } else {
            for (session in response.sessions) {
                dumpSleepSession(session, response.getDataSet(session))
            }
        }
    }

    private fun dumpSleepSession(session: Session, dataSets: List<DataSet>) {
        dumpSleepSessionMetadata(session)
        dumpSleepDataSets(dataSets)
    }

    private fun dumpSleepSessionMetadata(session: Session) {
        val (startDateTime, endDateTime) = getSessionStartAndEnd(session)
        val totalSleepForNight = calculateSessionDuration(session)

        binding.subView.textSleepDuration.text =
            "Sleep duration : \n${convertLongToSleepTime(totalSleepForNight)}"
        Log.e(TAG, "$startDateTime to $endDateTime ($totalSleepForNight mins)")
    }

    fun convertLongToSleepTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("HH 'hours', mm 'mins'")
        return format.format(date)
    }

    private fun dumpSleepDataSets(dataSets: List<DataSet>) {
        for (dataSet in dataSets) {
            for (dataPoint in dataSet.dataPoints) {
                val sleepStageOrdinal = dataPoint.getValue(Field.FIELD_SLEEP_SEGMENT_TYPE).asInt()
                val sleepStage = SLEEP_STAGES[sleepStageOrdinal]

                val durationMillis =
                    dataPoint.getEndTime(TimeUnit.MILLISECONDS) - dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                val duration = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
                Log.e(TAG, "\t$sleepStage: $duration (mins)")
            }
        }
    }


    private fun calculateSessionDuration(session: Session): Long {
        val total =
            session.getEndTime(TimeUnit.MILLISECONDS) - session.getStartTime(TimeUnit.MILLISECONDS)
        return TimeUnit.MILLISECONDS.toMillis(total)
    }

    private fun getSessionStartAndEnd(session: Session): Pair<String, String> {
        val dateFormat = DateFormat.getDateTimeInstance()
        val startDateTime = dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
        val endDateTime = dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS))
        return startDateTime to endDateTime
    }


    ///// Experiance 3D Music
    fun showBottomDialog() {

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.bottom_sheet_experiance_music)

        val btnContinue = dialog.findViewById<Button>(R.id.continue_btn)
        val btnCancel = dialog.findViewById<Button>(R.id.cancel_btn)

        btnContinue?.setOnClickListener {
            startActivity(Intent(this, HeadTrackingIntroActivity::class.java))
            dialog.dismiss()
        }
        btnCancel?.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

}

enum class FitActionRequestCode {
    SUBSCRIBE,
    READ_DATA,
    INSERT_AND_READ_DATA,
    UPDATE_AND_READ_DATA,
    DELETE_DATA,
    INSERT_SLEEP_SESSIONS,
    READ_SLEEP_SESSIONS
}

val SLEEP_STAGES = arrayOf(
    "Unused",
    "Awake (during sleep)",
    "Sleep",
    "Out-of-bed",
    "Light sleep",
    "Deep sleep",
    "REM sleep"
)