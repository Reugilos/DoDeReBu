package dodecagraphone.model.mixer;

import dodecagraphone.MyController;
import dodecagraphone.model.component.MyButton;
import dodecagraphone.model.component.MyButtonPanel;
import dodecagraphone.model.component.MyGridSquare;
import dodecagraphone.model.sound.SoundWithMidi;
import dodecagraphone.teclesControl.DeleteTrackSequence;
import dodecagraphone.ui.I18n;
import dodecagraphone.ui.Utilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class MyMixer {
    private int chordTrackId = 1003;
    private int drumsTrackId = 1005;
    private String chordTrackName = "ChordTrack";
    private String drumsTrackName = "DrumsTrack";
    
    private List<MyTrack> tracks;
    private MyTrack chordTrack;
    private MyTrack drumsTrack;
//    private List<Boolean> showFlags;
//    private List<Boolean> playFlags;
    private JPanel contentPanel;
    private JDialog dialog;
    private int currentTrack = -1;
    //private int nTracks;
    private int nChannels;
    private MyController contr;
    private boolean mixerVisible = false;
    private static MyMixer lastMixer;
    private JFrame parentFrame;
    private boolean modified = false;
//    private int chordTrack = -1;

    public MyMixer(MyController contr) {
        if (lastMixer != null) {
            lastMixer.closeMixerWindow();
        }
        lastMixer = this;
        this.contr = contr;
        this.contentPanel = new JPanel();
        this.contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        this.contentPanel.setDoubleBuffered(false);
        this.tracks = new ArrayList<>();
//        this.showFlags = new ArrayList<>();
//        this.playFlags = new ArrayList<>();
        //this.nTracks = 0;
        this.parentFrame = this.contr.getUi();
        this.chordTrack = null;
        this.drumsTrack = null;
    }

    public String getChordTrackName() {
        return chordTrackName;
    }

    public String getDrumsTrackName() {
        return drumsTrackName;
    }

    public int getChordTrackId() {
        return chordTrackId;
    }

    public int getDrumsTrackId() {
        return drumsTrackId;
    }

    public boolean isTrackVisible(int index) {
        MyTrack track = getTrackFromId(index);
        return track.isVisible();
    }

    public boolean isTrackAudible(int index) {
        MyTrack track = getTrackFromId(index);
        return track.isAudible();
    }

    public int getnTracks() {
        return tracks.size();
    }

    public void setChordTrack(MyTrack track) {
        this.chordTrack = track;
    }

    public void setDrumsTrack(MyTrack track) {
        this.drumsTrack = track;
    }

    public MyTrack getChordTrack() {
        return chordTrack;
    }

    public MyTrack getDrumsTrack() {
        return drumsTrack;
    }

    public int louder() {
        MyTrack tr = this.getCurrentTrack();
        int velocity = tr.getVelocity();
        velocity = Math.min(127, velocity + 10);
        tr.setVelocity(velocity);
        return velocity;
    }

    public int quieter() {
        MyTrack tr = this.getCurrentTrack();
        int velocity = tr.getVelocity();
        velocity = Math.max(0, velocity - 10);
        tr.setVelocity(velocity);
        return velocity;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public int getCurrentChannelOfCurrentTrack() {
        int index = currentTrack;
        MyTrack track = getTrackFromId(index);
        return track.getCurrentChannel();
    }

    public int getCurrentChannelOfTrack(int index) {
        MyTrack track = getTrackFromId(index);
        return track.getCurrentChannel();
    }

//    public void addTrack(int pos, MyTrack track, boolean show, boolean play, boolean isChordTrack) {
//        this.addTrack(pos, track, show, play);
//        if (isChordTrack) {
//            this.chordTrack = pos;
//        }
//    }
//
    public void addTrack(MyTrack track) {
        addTrack(tracks.size(), track);
    }

    private void addTrack(int pos, MyTrack track) {
        if (!tracks.contains(track)) {
            tracks.add(pos, track);
        }
    }

    public void closeMixerWindow() {
        if (dialog != null) {
            dialog.dispose();
            dialog = null;
            mixerVisible = false;
        }
    }

    public void refreshMixer() {
        if (SwingUtilities.isEventDispatchThread()) {
            doRefreshMixer();   // ja estem a l’EDT
        } else {
            SwingUtilities.invokeLater(this::doRefreshMixer);
        }
    }

    private void doRefreshMixer() {
        if (this.isMixerVisible()) {
            this.showMixer(); // recrea el diàleg
        }
        this.contr.getAllPurposeScore().drawCurrentCamInOffscreen();
        this.contr.getUi().getPanel().repinta(true); // pinta la graella
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        // [0] = Thread.getStackTrace
        // [1] = mètode actual
        // [2] = qui ha cridat el mètode actual
        StackTraceElement caller = stack[2];
        StackTraceElement caller2 = stack[4];
        Utilities.printOutWithPriority(false, "MyMixer::refreshMixer(): caller = " + caller2.getMethodName() + ":" + caller.getMethodName() + ": faig repinta(true)");
    }

    private boolean jaExisteixPistaAmbNom(String nom) {
        return tracks.stream().anyMatch(t -> t.getName().equalsIgnoreCase(nom));
    }

    public void showMixer() {
        Utilities.printOutWithPriority(false, "MyMixer::showMixer():");
        if (dialog != null) {
            dialog.dispose();
            dialog = null;
        }
        if (lastMixer != null) {
            lastMixer.closeMixerWindow();
        }
        lastMixer = this;

        dialog = new JDialog(parentFrame, "Mixer", false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

// 🔑 Afegim Key Binding per Ctrl+Z i Ctrl+Shift+Z
        InputMap inputMap = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = dialog.getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "redo");

        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contr.undo();
                refreshMixer();            
            }
        });
        actionMap.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contr.redo();
                refreshMixer();            
            }
        });

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                MyButtonPanel buttons = contr.getButtons();
                MyButton but = buttons.getButtons().get(buttons.getId_MixerButton());
                but.setPressed(false);
                buttons.setModified(true);
            }
        });

        contentPanel.removeAll();
        if (this.chordTrack != null) {
            addTrackToDialog(this.chordTrackId, true);
        }
        for (int i = 0; i < tracks.size(); i++) {
            addTrackToDialog(i, false);
        }
        if (this.drumsTrack != null) {
            addTrackToDialog(this.drumsTrackId, true);
        }

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(contentPanel);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        int maxTrackNumber = tracks.stream()
                .map(MyTrack::getName)
                .filter(n -> n.toLowerCase().startsWith("track "))
                .map(n -> {
                    try {
                        return Integer.parseInt(n.substring(6).trim());
                    } catch (NumberFormatException ex) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(0);

        final int[] trackNameCounter = {maxTrackNumber + 1};

        JButton markButton = new JButton();
        Runnable updateMarkButtonLabel = () -> {
            MyTrack current = getCurrentTrack();
            if (current != null) {
                markButton.setText(current.isDotted() ? "Unmark" : "Mark");
            } else {
                markButton.setText("Mark");
            }
        };
        updateMarkButtonLabel.run();

        markButton.addActionListener(e -> {
            MyTrack current = getCurrentTrack();
            if (current != null) {
                current.toggleDotted();
                updateMarkButtonLabel.run();
                refreshMixer();
            } else {
                JOptionPane.showMessageDialog(null, "Cap pista seleccionada.");
            }
        });

        JButton renameButton = new JButton("Rename");
        renameButton.addActionListener(e -> {
            MyTrack current = getCurrentTrack();
            if (current == null) {
                JOptionPane.showMessageDialog(null, "Cap pista seleccionada.");
                return;
            }

            String nomActual = current.getName();
            String nouNom = (String) JOptionPane.showInputDialog(
                    null,
                    "Nom de la pista:",
                    "Canvia el nom",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    nomActual
            );

            if (nouNom != null) {
                nouNom = nouNom.trim();
                if (nouNom.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "El nom no pot ser buit.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (jaExisteixPistaAmbNom(nouNom)) {
                    JOptionPane.showMessageDialog(null, "Ja existeix una pista amb aquest nom.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                current.setName(nouNom);
                refreshMixer();
            }
        });

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> {
            int id = getCurrentTrackId();
            MyTrack current = getCurrentTrack();
            if (current == null) {
                JOptionPane.showMessageDialog(null, "Cap pista seleccionada.");
                return;
            }

            boolean buida = current.isEmpty();
            boolean confirmat = true;

            if (!buida) {
                int resposta = JOptionPane.showConfirmDialog(
                        null,
                        "La pista conté notes. Segur que vols eliminar-la?",
                        "Confirmació",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                confirmat = resposta == JOptionPane.YES_OPTION;
            }

            if (confirmat) {
                List<MyGridSquare.SubSquare> list = this.contr.getAllPurposeScore().getNotesOfTrack(id);
                
                DeleteTrackSequence sequence = new DeleteTrackSequence(this.contr);
                
                if (sequence!=null) {
                    sequence.addAllChanges(id,list);
                    this.contr.afegirEvent(sequence);
                }
                this.contr.getAllPurposeScore().removeNotesOfList(list);
                // Eliminar la pista
                markTrackAsDeletedAndResetCurrentTrack(id);
                // Refrescar UI
                refreshMixer();
            }
        });

        JButton addTrackButton = new JButton("+ Add Track");
        addTrackButton.addActionListener(e -> {
            String nom;
            do {
                nom = JOptionPane.showInputDialog(null, "Nom de la nova pista:", "Track " + trackNameCounter[0]);
                if (nom == null) {
                    return;
                }
                nom = nom.trim();

                boolean jaExisteix = jaExisteixPistaAmbNom(nom);

                if (jaExisteix) {
                    JOptionPane.showMessageDialog(null, "Ho sento, ja hi ha una pista amb aquest nom.", "Error", JOptionPane.ERROR_MESSAGE);
                    nom = null;
                }
            } while (nom == null || nom.isEmpty());

            trackNameCounter[0]++;

            MyTrack nova = new MyTrack(tracks.size(), nom);
            nova.setIsNew(true);
            this.contr.addTrackAndInstrumentToMixer(nova,SoundWithMidi.getLeadInstrument());
            refreshMixer();
            updateMarkButtonLabel.run();
        });

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footerPanel.add(markButton);
        footerPanel.add(renameButton);
        footerPanel.add(deleteButton);
        footerPanel.add(addTrackButton);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(footerPanel);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.setBackground(Color.WHITE);
        dialog.setOpacity(1.0f);

        dialog.pack();
        dialog.setLocation(0, 0);
        // dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        this.modified = true;
        SwingUtilities.invokeLater(() -> {
            dialog.revalidate();
            dialog.repaint();
        });
        parentFrame.revalidate();
        parentFrame.repaint();
        mixerVisible = true;
    }

    private void addTrackToDialog(int index, boolean alwaysShow) {
        MyTrack track = getTrackFromId(index);
        if (track == null || track.isDeleted()) return;
        if (!alwaysShow && track.getnNotes() <= 0 && !track.isIsNew()) return;
        this.modified = true;

        JPanel panelPista = new JPanel(new BorderLayout(10, 10));

        JButton showButton = new JButton();
        updateButtonState(showButton, index, true);
        showButton.addActionListener(e -> {
            track.setVisible(!track.isVisible());
            updateButtonState(showButton, index, true);
            contr.update(); // refresca la graella
            refreshMixer(); // refresca el diàleg del mixer (opcional)
        });

        JButton playButton = new JButton();
        updateButtonState(playButton, index, false);
        playButton.addActionListener(e -> {
            track.setAudible(!track.isAudible());
            updateButtonState(playButton, index, false);
        });

        String prefix = track.isSelected() ? ">> " : "";
        String displayName;
        if (index == chordTrackId) displayName = I18n.t("mixer.chordTrack");
        else if (index == drumsTrackId) displayName = I18n.t("mixer.drumsTrack");
        else displayName = track.getName();
        JLabel labelInfo = new JLabel(prefix + displayName + " (" + track.getId() + "): "
                + track.toStringCanalsInstruments() + ", " + track.getVelocity() + ", " + track.getnNotes());
        labelInfo.setBorder(new EmptyBorder(0, 10, 0, 0));

// Panell esquerra: show, play, label
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.add(showButton);
        leftPanel.add(playButton);
        leftPanel.add(labelInfo);

// Panell dreta: select (no per drums)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        if (index != drumsTrackId) {
            JButton selectButton = new JButton(track.isSelected() ? "Selected" : "Select");
            selectButton.addActionListener(e -> {
                setCurrentTrack(index);
                refreshMixer();
            });
            rightPanel.add(selectButton);
        }

// Panell combinat amb BorderLayout
        JPanel filaCompleta = new JPanel(new BorderLayout());
        filaCompleta.add(leftPanel, BorderLayout.CENTER);
        filaCompleta.add(rightPanel, BorderLayout.EAST);

// Afegir al panell de la pista
        panelPista.add(filaCompleta, BorderLayout.CENTER);

        contentPanel.add(panelPista);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void updateButtonState(JButton button, int index, boolean isShow) {
        MyTrack track = getTrackFromId(index);
        if (isShow) {
            boolean state = track.isVisible();
            button.setText(state ? "Show" : "Hide ");
        } else {
            boolean state = track.isAudible();
            button.setText(state ? "Play" : "Mute");
        }
    }
    
    public MyTrack getTrackFromId(int index){
        MyTrack track;
        if (index == this.getChordTrackId()){
            track = this.getChordTrack();
        } else if (index == this.getDrumsTrackId()){
            track = this.getDrumsTrack();
        } else { 
            track = tracks.get(index);
        }
        return track;
    }

    public boolean isMixerVisible() {
        return mixerVisible;
    }

    public void setCurrentTrack(int id) {
        this.currentTrack = id;
        for (int i = 0; i < tracks.size(); i++) {
            tracks.get(i).setSelected(i == id);
        }
        if (chordTrack != null) chordTrack.setSelected(id == this.getChordTrackId());
        if (drumsTrack != null) drumsTrack.setSelected(id == this.getDrumsTrackId());
    }

    public MyTrack getCurrentTrack() {
        int index = currentTrack;
        if (index <0) return null;
        MyTrack track = getTrackFromId(index);
        return track;
    }

    public int getCurrentTrackId() {
        return currentTrack;
    }

    public List<MyTrack> getTracks() {
        return tracks;
    }
    
    public void markTrackAsDeletedAndResetCurrentTrack(int id) {
        if (id < 0 || id >= tracks.size()) {
            return;
        }

        tracks.get(id).setDeleted(true);
//        showFlags.remove(id);
//        playFlags.remove(id);
//        nTracks--;

        // Reajustem l'índex de la pista seleccionada
        int n = tracks.size()-1;
        while (n>=0 && tracks.get(n).isDeleted()) n--;
        currentTrack = n;
//        else if (currentTrack > id) {
//            currentTrack--;
//        }

        // Reassignem ID als MyTrack restants si és important
        for (int i = 0; i < tracks.size(); i++) {
            tracks.get(i).setSelected(i == currentTrack);
        }
    }

}
