package cz.Empatix.Utility;

/**
 * @author Jigar
 */

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class CopyImagetoClipBoard implements ClipboardOwner {
    public void copyToClipboard() {
        try {
            Robot robot = new Robot();
            Dimension screenSize  = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screen = new Rectangle( screenSize );
            BufferedImage i = robot.createScreenCapture( screen );
            TransferableImage trans = new TransferableImage(i);
            Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
            c.setContents( trans, this );
        }
        catch ( AWTException x ) {
            x.printStackTrace();
        }
    }

    public void lostOwnership( Clipboard clip, Transferable trans ) {
    }
    public void copyImage(BufferedImage bi)
    {
        TransferableImage trans = new TransferableImage(bi);
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        c.setContents( trans, this );
    }
    public String getStringCopy(){
        Clipboard c= Toolkit.getDefaultToolkit().getSystemClipboard();
        try{
           return (String)c.getData(DataFlavor.stringFlavor);
        } catch (Exception e){
            return null;
        }
    }
    private static class TransferableImage implements Transferable {

        Image i;

        public TransferableImage( Image i ) {
            this.i = i;
        }

        public Object getTransferData( DataFlavor flavor )
                throws UnsupportedFlavorException, IOException {
            if ( flavor.equals( DataFlavor.imageFlavor ) && i != null ) {
                return i;
            }
            else {
                throw new UnsupportedFlavorException( flavor );
            }
        }

        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = new DataFlavor[ 1 ];
            flavors[ 0 ] = DataFlavor.imageFlavor;
            return flavors;
        }

        public boolean isDataFlavorSupported( DataFlavor flavor ) {
            DataFlavor[] flavors = getTransferDataFlavors();
            for ( int i = 0; i < flavors.length; i++ ) {
                if ( flavor.equals( flavors[ i ] ) ) {
                    return true;
                }
            }

            return false;
        }
    }
}
