package com.example.send_encrypted_cc_hce;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.example.send_encrypted_cc_hce.IsoDepTransceiver.OnMessageReceived;

public class MainActivity extends Activity implements OnMessageReceived, ReaderCallback {

	private NfcAdapter nfcAdapter;
	private ListView listView;
	private IsoDepAdapter isoDepAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		isoDepAdapter = new IsoDepAdapter(getLayoutInflater());
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
				null);
	}

	@Override
	public void onPause() {
		super.onPause();
		nfcAdapter.disableReaderMode(this);
	}

	@Override
	public void onTagDiscovered(Tag tag) {
		IsoDep isoDep = IsoDep.get(tag);
		IsoDepTransceiver transceiver = new IsoDepTransceiver(isoDep, this);
		Thread thread = new Thread(transceiver);
		thread.start();
	}

	@Override
	public void onMessage(final byte[] message) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				isoDepAdapter.addMessage(new String(message));
			}
		});
	}
	
	public void goToScan(View v){
		Intent i = new Intent(getApplicationContext(), ReadCard.class);
		startActivity(i);
	}
	
	/*public void goToSend(View v){
		Intent i = new Intent(getApplicationContext(), SendCard.class);
		startActivity(i);
	}
	
	public void goToReceive(View v){
		
		Intent i = new Intent(ReceiveCard.class);
		
	}*/

	@Override
	public void onError(Exception exception) {
		onMessage(exception.getMessage().getBytes());
	}
}
