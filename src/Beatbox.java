import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Beatbox {
    JPanel mainPanel;
    ArrayList<JCheckBox> checkboxList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame theFrame;
    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat","Open Hi-Hat", "Acoustic Snare", "Crash Cymbal","Hand Clap","Cowbell", "Vibraslap","Low-mid Tom","High Agogo","Open Hi Conga"};
    int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
    public static void main(String[] args) {
       new Beatbox().buildGUI();
    }
    public void buildGUI(){
        theFrame = new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel();
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkboxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        start.addActionListener(new MyStopListner());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton save = new JButton("Save");
        save.addActionListener(new SaveActionListener());
        buttonBox.add(save);

        JButton restore = new JButton("Restore");
        restore.addActionListener(new RestoreActionListener());
        buttonBox.add(restore);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for(int i = 0 ; i < instrumentNames.length ; i++){
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.WEST,buttonBox);
        background.add(BorderLayout.CENTER,nameBox);

        theFrame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.EAST,mainPanel);
        
        for( int i = 0; i < 256 ; i++){
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
        }
        
        setUpMidi();

        theFrame.setBounds(50,50,300,300);
        theFrame.pack();
        theFrame.setVisible(true);
        theFrame.setVisible(true);

    }
    public void setUpMidi(){
        try{
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track =sequence.createTrack();
            sequencer.setTempoInBPM(120);
        }catch(Exception e){
            e.printStackTrace();
        }

        }
    public void buildTrackAndStart(){
        int[] trackList = null;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for(int i = 0 ; i < 16 ; i++){
            trackList =new int[16];

            int key = instruments[i];
                for( int j = 0 ; j < 16 ; j++){
                    JCheckBox jc = (JCheckBox) checkboxList.get(j+(16*i));//not clear
                    if( jc.isSelected()){
                        trackList[j] = key;

                    }else{
                        trackList[j]= 0;

                    }
                }
            makeTracks(trackList);
            track.add(makeEvent(192,9,1,0,15));
        }
            try{
                sequencer.setSequence(sequence);
                sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
                sequencer.setTempoInBPM(120);
                sequencer.start();
            }catch(Exception e){
                e.printStackTrace();
            }

        }

    public class MyStartListener implements ActionListener{
        public void actionPerformed(ActionEvent a){
            buildTrackAndStart();
        }
    }
    public class MyStopListner implements ActionListener{
        public void actionPerformed(ActionEvent a){
            sequencer.stop();
        }
    }
    public class MyUpTempoListener implements ActionListener{
        public void actionPerformed(ActionEvent a){
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor*1.03));

        }
    }
    public class MyDownTempoListener implements ActionListener{
        public void actionPerformed(ActionEvent a){
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor*0.97));
            
        }
    }
    public class SaveActionListener implements ActionListener{
        public void actionPerformed(ActionEvent event){
            boolean[] checkboxState = new boolean[256];
            for(int i = 0 ; i < 256; i++){
                JCheckBox check = (JCheckBox)checkboxList.get(i);
                if(check.isSelected()){
                    checkboxState[i] = true;
                    
                }
            }
            try {
                FileOutputStream out = new FileOutputStream("BeatyBeaty.ser");
                ObjectOutputStream o = new ObjectOutputStream(out);
                o.writeObject(checkboxState);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
    }
    public class RestoreActionListener implements ActionListener{
        public void actionPerformed(ActionEvent event){
            boolean [] checkboxState = null;
            try {
                FileInputStream in = new FileInputStream("BeatyBeaty.ser");
                ObjectInputStream is = new ObjectInputStream(in);
                checkboxState = (boolean[]) is.readObject();
                
            } catch (Exception e) {
                buildTrackAndStart();
            }
            for( int i = 0 ; i < 256 ; i++){
                JCheckBox check = (JCheckBox)checkboxList.get(i);
                if(checkboxState[i]){
                    check.setSelected(true);
                }else{
                    check.setSelected(false);
                }
            } 
        }
    }

    public void makeTracks(int [] list){
        for(int i = 0 ; i < 16 ; i ++){
            int key = list[i];

            if(key!=0){
                track.add(makeEvent(144,9,key,100,i));
                track.add(makeEvent(128,9,key,100,i+1));

            }
        }
    }
    public MidiEvent makeEvent(int comd, int chan , int one , int two , int tick){
        MidiEvent event = null;
        try{
            ShortMessage a = new ShortMessage();
            a.setMessage(comd,chan,one,two);
            event = new MidiEvent(a, tick);

        }catch(Exception e){
            e.printStackTrace();
            
        } return event;
    }

}
    
    

