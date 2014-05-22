package com.example.send_encrypted_cc_hce;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

public class MyHostApduService extends HostApduService {

	private int messageCounter = 0;
	private final int key_length = 128;
	private final String ALGO = "AES";
	private byte[] key;
	private byte[] cc;

	public void setCC(byte[] cc) {
		this.cc = cc;
	}

	@Override
	public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
		
		if (isAESApdu(apdu) & apdu.length == (key_length + 6) && isCCN()) {
			Log.i("HCEDEMO", "Application selected");
			System.arraycopy(apdu, 6, key, 0, key_length);
			
			//gets cc from sharedpreferences
			cc = getCC();
			
			
			//encrypts the ccn
			byte[] response = encryptMessage(key, cc);
			
			byte[] myapdu = new byte[134];
			
			System.arraycopy(apdu, 0, myapdu, 0, 6);
			
			System.arraycopy(response, 0,myapdu, 6, 128);

			sendResponseApdu(myapdu);

		}
		return getNextMessage();
	}

	private boolean isCCN() {
		SharedPreferences sp = getSharedPreferences("cc", MODE_PRIVATE);

		String ccn = sp.getString("ccn", "1111111111111111111");

		return ccn != "1111111111111111111";

	}

	private byte[] getCC() {
		cc = new byte[128];
		SharedPreferences sp = getSharedPreferences("cc", MODE_PRIVATE);

		System.arraycopy(sp.getString("ccn", "1111111111111111111"), 0, cc, 0,
				19);

		System.arraycopy(sp.getString("name", "John Doe"), 0, cc, 19, 20);

		System.arraycopy(sp.getInt("month", 9), 0, cc, 39, 4);

		System.arraycopy(sp.getInt("year", 2013), 0, cc, 43, 4);

		System.arraycopy(sp.getInt("cvv", 2222), 0, cc, 47, 4);

		//pads out message for aes128
		for(int i = 51; i < 128; i++)
			cc[i]=0;
		
		return cc;
	}

	private byte[] encryptMessage(byte[] cipher, byte[] data) {
		Key key = new SecretKeySpec(cipher, ALGO);
		byte[] encval = {(byte)0xa4};
		try {
			Cipher c = Cipher.getInstance(ALGO);
			c.init(Cipher.ENCRYPT_MODE, key);
			encval = c.doFinal(data);
		} catch (Exception e) {
			
		}

		return encval;

	}

	private byte[] getWelcomeMessage() {
		return "Hello Desktop!".getBytes();
	}

	private byte[] getNextMessage() {
		return ("Message from android: " + messageCounter++).getBytes();
	}

	private boolean isAESApdu(byte[] apdu) {
		return apdu.length >= 2 && apdu[0] == (byte) 0
				&& apdu[1] == (byte) 0xa4;

	}

	private boolean selectAidApdu(byte[] apdu) {
		return apdu.length >= 2 && apdu[0] == (byte) 0
				&& apdu[1] == (byte) 0xa4;
	}

	@Override
	public void onDeactivated(int reason) {
		Log.i("HCEDEMO", "Deactivated: " + reason);
	}
}