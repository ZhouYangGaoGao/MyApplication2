package com.idcard.modernskyticketing1;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.huashi.lua.HS550;

/**
 * Created by LUA on 2016/9/8.
 */
public class HsUtlis {

    private static HS550 hs550;

//    static {
//        hs550 = new HS550();
//        device = new Device();
//    }
    
    public static int UsbPonwer1() {
		// TODO Auto-generated method stub
		if (hs550 == null) {
			hs550 = new HS550();
		}
		return hs550.PowerOnUSB();
	}

	public static int UsbPonoff1() {
		// TODO Auto-generated method stub
		if (hs550 == null) {
			hs550 = new HS550();
		}
		return hs550.PowerOffUSB();
	}

    public static void IDCardPonwer1() throws IOException {
        writeFile("-wdout5 1");
        writeFile("-wdout13 1");
    }

    public static void IDCardPonoff1() throws IOException {
        writeFile("-wdout5 0");
        writeFile("-wdout13 0");
    }

    private static void writeFile(String sData2Write) throws IOException {

        if (sData2Write == null){
//            Log.e(TAG, "no data write");
            return;
        }
        String sFile = "/sys/class/misc/mtgpio/pin";
        String sdata = sData2Write ;
        File afile = new File(sFile);

        FileOutputStream out = new FileOutputStream(afile, false);
        try {
            out.write( sdata.getBytes());
            Thread.sleep(50);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
