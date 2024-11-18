package tec.mx.ocoyucango.presentation.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            Log.e("Geofence", "Geofence error: ${geofencingEvent?.errorCode}")
            return
        }

        // Verificar la transición
        val geofenceTransition = geofencingEvent.geofenceTransition
        val viewModel = RouteViewModel(context.applicationContext as Application)

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d("Geofence", "Entered geofence")
            viewModel.updateGeofenceStatus(true)
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.d("Geofence", "Exited geofence")
            viewModel.updateGeofenceStatus(false)
        }
    }
}
