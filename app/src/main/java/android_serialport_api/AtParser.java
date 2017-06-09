package android_serialport_api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class AtParser  {

    /**
     * Serial I/O
     */
    private OutputStream outStream;
    private LineNumberReader inStreamReader;

    /**
     * AT parser phases: IDLE or parsing AT command's data
     */
    private enum ParserPhase {

        IDLE, DATA
    }
    private ParserPhase currPhase;
    /**
     * The AT command's data
     */
    ArrayList atCmdData;

    /**
     * The AT command's status: OK or ERROR
     */
    public enum AtCmdStatus {

        OK, ERROR
    }
    private AtCmdStatus currStatus;
    /**
     * Flag to indicate whether the parsing is done or not
     */
    private boolean doneFlag;

    public AtParser(InputStream inStream, OutputStream outStream) {
        inStreamReader =
                new LineNumberReader(new InputStreamReader(inStream));
        this.outStream = outStream;

        // Initialize the parser phase
        currPhase = ParserPhase.IDLE;
    }

    /**
     * Send AT command to the serial port
     *
     * @param atCmd The AT command (ended with r or )
     */
    public void sendAtCmd(String atCmd) throws IOException {
        outStream.write(atCmd.getBytes());
        doneFlag = false;
    }

    /**
     * Get the AT command's data
     *
     * @return Array of string consist of the AT command data
     */
    public String[] getAtCmdData() {
        ArrayList cmdData = this.atCmdData;

        // Clear the data for next instruction
        this.atCmdData = null;

        return (String[]) cmdData.toArray(new String[0]);
    }

    /**
     * Get the status of the executed AT command
     *
     * @return AtCmdStatus
     */
    public AtCmdStatus getAtCmdStatus() {
        return currStatus;
    }

    /**
     * Get the parser status
     *
     * @return true if parsing is done, false otherwise
     */
    public boolean isParsingDone() {
        return doneFlag;
    }

    private void readLine() {
        try {
            while (inStreamReader.ready()) {
                String newline = inStreamReader.readLine();

                // Skip empty lines
                if (newline.equals("")) {
                    continue;
                }

                if (currPhase == ParserPhase.IDLE) {
                    if (newline.startsWith("AT")) {
                        currPhase = ParserPhase.DATA;
                    } else {
                        handleUrc(newline);
                    }
                } else if (currPhase == ParserPhase.DATA) {
                    if (newline.startsWith("OK")) {
                        currPhase = ParserPhase.IDLE;
                        currStatus = AtCmdStatus.OK;

                        doneFlag = true;
                    } else if ((newline.startsWith("ERROR")) ||
                            (newline.startsWith("+CME ERROR")) ||
                            (newline.startsWith("+CMS ERROR"))) {
                        currPhase = ParserPhase.IDLE;
                        currStatus = AtCmdStatus.ERROR;

                        doneFlag = true;
                    } else {
                        if (atCmdData == null) {
                            atCmdData = new ArrayList();
                        }
                        atCmdData.add(newline);
                    }
                }

                //System.out.println("Recv :" + newline);
                //System.out.println("currPhase :" + currPhase);
            }
        } catch (IOException ex) {
        }
    }

    /**
     * Handle the URC from the modem
     *
     * @param urc
     */
    protected void handleUrc(String urc) {
        System.out.println("Received URC :" + urc);
    }

    /**
     * This is the embedded static class to test this function
     *
    public static class AtParserTester {

        private static final String serialPort = "/dev/ttyACM0";

        public static void main(String[] args) {
            AtParser atParser = null;
            SerialHelper serialHelper = new SerialHelper();

            try {
                serialHelper.connect(AtParserTester.serialPort);

                InputStream inStream = serialHelper.getSerialInputStream();
                OutputStream outStream = serialHelper.getSerialOutputStream();

                atParser = new SimpleAtParser(inStream, outStream);
                serialHelper.addDataAvailableListener(atParser);
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
                System.exit(1);
            } catch (TooManyListenersException ex) {
                System.err.println(ex.getMessage());
                System.exit(1);
            }

            if (atParser != null) {
                try {
                    atParser.sendAtCmd("AT+CMGL=4r");

                    // Wait until the parsing is done
                    while (!atParser.isParsingDone()) {
                        // Sleep for 1-s
                        Thread.sleep(10);
                    }

                    if (atParser.getAtCmdStatus() == AtCmdStatus.OK) {
                        for (String atCmdData : atParser.getAtCmdData()) {
                            System.out.println("Data :" + atCmdData);
                        }
                    } else {
                        System.out.println("AT Command Error");
                    }

                } catch (InterruptedException ex) {
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                    System.exit(1);
                }
            }

            serialHelper.disconnect();
        }
    }
     */
}