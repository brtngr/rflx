package com.arkeglorus.dreamsight

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import java.util.*

class LeManager(context: Context) : BleManager(context) {
    private var write_char: BluetoothGattCharacteristic? = null
    private var notify_char: BluetoothGattCharacteristic? = null
    private var listener: OnNotifyListener? = null
    fun setNotifyListener(listener: OnNotifyListener?) {
        this.listener = listener
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return MyManagerGattCallback()
    }

    fun write(text: String) {
        val textToSend = text.replace('\u00a0',' ').toByteArray()+0
        writeCharacteristic(write_char, textToSend,BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
            .split()
            .done { }
            .enqueue()
        Log.d("Dreamsight", "Send: ${text.replace('\u00a0',' ')}")
        Log.d("Dreamsight", "len: ${text.length}")
    }

    override fun log(priority: Int, message: String) {

    }


    interface OnNotifyListener {
        fun onNotify(device: BluetoothDevice?, data: Data?)
    }

    /**
     * BluetoothGatt callbacks object.
     */
    private inner class MyManagerGattCallback : BleManagerGattCallback() {
        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(service_uuid)
            if (service != null) {
                write_char = service.getCharacteristic(write_uuid)
                //notify_char = service.getCharacteristic(notify_uuid)
            }
            var writeRequest = false
            if (write_char != null) {
                val properties = write_char!!.properties
                writeRequest =
                    properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0
                write_char!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            }
            // Return true if all required services have been found
            return write_char != null&& writeRequest// && notify_char != null
        }

        // If you have any optional services, allocate them here. Return true only if
        // they are found.
        override fun isOptionalServiceSupported(gatt: BluetoothGatt): Boolean {
            return super.isOptionalServiceSupported(gatt)
        }

        override fun initialize() {
            requestMtu(517)
                .enqueue()
        }

        override fun onDeviceDisconnected() {
            // Device disconnected. Release your references here.
            write_char = null
            notify_char = null
        }

        override fun onServicesInvalidated() {

        }
    }

    companion object {
        val service_uuid: UUID = UUID.fromString("01001523-1212-EFDE-1523-785FEABCD123")
        val write_uuid: UUID = UUID.fromString("01001525-1212-EFDE-1523-785FEABCD123")
        val notify_uuid: UUID = UUID.fromString("01001524-1212-EFDE-1523-785FEABCD123")
    }
}
