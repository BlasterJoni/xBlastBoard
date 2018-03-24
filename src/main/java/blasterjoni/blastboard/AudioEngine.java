package blasterjoni.blastboard;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import javax.sound.sampled.*;
import java.io.IOException;

//TODO: Change this to VLCj
public class AudioEngine {

    private final Mixer.Info[] mixers = AudioSystem.getMixerInfo();
    
    private final AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
    private final DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
    
    private boolean playing = false;
    
    private Mixer firstOutput = AudioSystem.getMixer(mixers[0]);
    private double firstVolume = 1D;
    
    private Mixer secondOutput = AudioSystem.getMixer(mixers[0]);
    private double secondVolume = 1D;

    public AudioEngine(String firstOutName, String secondOutName, Double firstVOL, Double secondVOL){
        for (Mixer.Info a : mixers) {
            Mixer mixer = AudioSystem.getMixer(a);
            if (a.getName().equals(firstOutName)){
                firstOutput = mixer;
            }
            if (a.getName().equals(secondOutName)){
                secondOutput = mixer;
            }
        }
        
        firstVolume = firstVOL / 100;
        secondVolume = secondVOL / 100;
    }

    public void setFirstVolume(Double volume){
        firstVolume = volume / 100;
        float value = (float)(Math.log(firstVolume) / Math.log(10.0) * 20.0);
        for (Object obj : firstOutput.getSourceLines()) {
            SourceDataLine SourceLine = (SourceDataLine)obj;
            FloatControl firstGainControl = (FloatControl) SourceLine.getControl(FloatControl.Type.MASTER_GAIN);
            firstGainControl.setValue(value);
        }
    }
    
    public void setSecondVolume(Double volume){
        secondVolume = volume / 100;
        float value = (float)(Math.log(secondVolume) / Math.log(10.0) * 20.0);
        for (Object obj : secondOutput.getSourceLines()) {
            SourceDataLine SourceLine = (SourceDataLine)obj;
            FloatControl firstGainControl = (FloatControl) SourceLine.getControl(FloatControl.Type.MASTER_GAIN);
            firstGainControl.setValue(value);
        }
    }
    
    public void stop(){
        playing = false;
    }

    public void play(String path){
        playing = true;
        try{
            //Creating lines
            SourceDataLine firstSourceLine = (SourceDataLine)firstOutput.getLine(info);
            SourceDataLine secondSourceLine = (SourceDataLine)secondOutput.getLine(info);
            firstSourceLine.open();
            secondSourceLine.open();
            firstSourceLine.start();
            secondSourceLine.start();
            
            //Setting volume -- Here be dragons!
            FloatControl firstGainControl = (FloatControl) firstSourceLine.getControl(FloatControl.Type.MASTER_GAIN);
            firstGainControl.setValue((float)(Math.log(firstVolume) / Math.log(10.0) * 20.0));
            FloatControl secondGainControl = (FloatControl) secondSourceLine.getControl(FloatControl.Type.MASTER_GAIN);
            secondGainControl.setValue((float)(Math.log(secondVolume) / Math.log(10.0) * 20.0));

            //Run threads that write the data from the AudioInputStream to the lines
            Runnable firstWAISTL = new WriteAudioInputStreamToLine(path, firstSourceLine);
            new Thread(firstWAISTL).start();
            Runnable secondWAISTL = new WriteAudioInputStreamToLine(path, secondSourceLine);
            new Thread(secondWAISTL).start();
        }
        catch (LineUnavailableException lue){
            lue.printStackTrace();
        }
    }

    //Here be dragons!
    private class WriteAudioInputStreamToLine implements Runnable {

        String path;
        SourceDataLine sourceLine;
        int outputNumber;

        public WriteAudioInputStreamToLine(String path, SourceDataLine sourceLine){
            this.path = path;
            this.sourceLine = sourceLine;
        }

        int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
        byte buffer[] = new byte[bufferSize];

        @Override
        public void run() {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path))){
                AudioInputStream audioStream  = AudioSystem.getAudioInputStream(bis);
                int count;
                while ((count = audioStream.read(buffer, 0, buffer.length)) != -1) {
                    if (count > 0)
                        sourceLine.write(buffer, 0, count);
                    if (!playing)
                        break;
                }
                if(playing)
                    sourceLine.drain(); //This play what's in buffer till the end
                sourceLine.close();
            }
            catch (UnsupportedAudioFileException | IOException ex){
                ex.printStackTrace();
            }

        }
    }

}
