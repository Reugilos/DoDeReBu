/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
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

/**
 * [CA] Mesclador de pistes de la partitura. Gestiona la llista de pistes
 * ({@link MyTrack}), la pista d'acords i la de percussió, i proporciona
 * una finestra de diàleg Swing per controlar la visibilitat, l'audibilitat,
 * el nom i la selecció de cada pista. Suporta undo/redo via Ctrl+Z/Ctrl+Shift+Z
 * des de la finestra del mesclador. Només un mesclador pot estar obert a la vegada.
 * <p>
 * [EN] Score track mixer. Manages the list of tracks ({@link MyTrack}),
 * the chord track and the drums track, and provides a Swing dialog window
 * to control each track's visibility, audibility, name and selection.
 * Supports undo/redo via Ctrl+Z/Ctrl+Shift+Z from the mixer window.
 * Only one mixer can be open at a time.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
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

    /**
     * [CA] Crea un nou mesclador associat al controlador indicat. Si hi havia un
     * mesclador anterior obert, el tanca primer. Inicialitza la llista de pistes
     * buida i el panell de continguts.
     * <p>
     * [EN] Creates a new mixer associated with the given controller. If there was
     * a previously open mixer, it is closed first. Initializes the empty track list
     * and the content panel.
     *
     * @param contr [CA] referència al controlador principal / [EN] reference to the main controller
     */
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

    /**
     * [CA] Retorna el nom intern de la pista d'acords.
     * <p>
     * [EN] Returns the internal name of the chord track.
     *
     * @return [CA] nom de la pista d'acords / [EN] chord track name
     */
    public String getChordTrackName() {
        return chordTrackName;
    }

    /**
     * [CA] Retorna el nom intern de la pista de percussió.
     * <p>
     * [EN] Returns the internal name of the drums track.
     *
     * @return [CA] nom de la pista de percussió / [EN] drums track name
     */
    public String getDrumsTrackName() {
        return drumsTrackName;
    }

    /**
     * [CA] Retorna l'identificador especial de la pista d'acords.
     * <p>
     * [EN] Returns the special identifier of the chord track.
     *
     * @return [CA] id de la pista d'acords / [EN] chord track id
     */
    public int getChordTrackId() {
        return chordTrackId;
    }

    /**
     * [CA] Retorna l'identificador especial de la pista de percussió.
     * <p>
     * [EN] Returns the special identifier of the drums track.
     *
     * @return [CA] id de la pista de percussió / [EN] drums track id
     */
    public int getDrumsTrackId() {
        return drumsTrackId;
    }

    /**
     * [CA] Indica si la pista amb l'índex donat és visible a la partitura.
     * <p>
     * [EN] Returns whether the track at the given index is visible in the score.
     *
     * @param index [CA] identificador de la pista / [EN] track identifier
     * @return {@code true} si la pista és visible / {@code true} if the track is visible
     */
    public boolean isTrackVisible(int index) {
        MyTrack track = getTrackFromId(index);
        return track.isVisible();
    }

    /**
     * [CA] Indica si la pista amb l'índex donat és audible durant la reproducció.
     * <p>
     * [EN] Returns whether the track at the given index is audible during playback.
     *
     * @param index [CA] identificador de la pista / [EN] track identifier
     * @return {@code true} si la pista és audible / {@code true} if the track is audible
     */
    public boolean isTrackAudible(int index) {
        MyTrack track = getTrackFromId(index);
        return track.isAudible();
    }

    /**
     * [CA] Retorna el nombre total de pistes (excloent les pistes especials
     * d'acords i percussió).
     * <p>
     * [EN] Returns the total number of tracks (excluding the special chord
     * and drums tracks).
     *
     * @return [CA] nombre de pistes / [EN] number of tracks
     */
    public int getnTracks() {
        return tracks.size();
    }

    /**
     * [CA] Estableix la pista d'acords del mesclador.
     * <p>
     * [EN] Sets the chord track of the mixer.
     *
     * @param track [CA] pista d'acords / [EN] chord track
     */
    public void setChordTrack(MyTrack track) {
        this.chordTrack = track;
    }

    /**
     * [CA] Estableix la pista de percussió del mesclador.
     * <p>
     * [EN] Sets the drums track of the mixer.
     *
     * @param track [CA] pista de percussió / [EN] drums track
     */
    public void setDrumsTrack(MyTrack track) {
        this.drumsTrack = track;
    }

    /**
     * [CA] Retorna la pista d'acords del mesclador.
     * <p>
     * [EN] Returns the chord track of the mixer.
     *
     * @return [CA] pista d'acords, o {@code null} si no n'hi ha /
     *         [EN] chord track, or {@code null} if none
     */
    public MyTrack getChordTrack() {
        return chordTrack;
    }

    /**
     * [CA] Retorna la pista de percussió del mesclador.
     * <p>
     * [EN] Returns the drums track of the mixer.
     *
     * @return [CA] pista de percussió, o {@code null} si no n'hi ha /
     *         [EN] drums track, or {@code null} if none
     */
    public MyTrack getDrumsTrack() {
        return drumsTrack;
    }

    /**
     * [CA] Incrementa la velocitat de la pista seleccionada en 10 unitats
     * (màxim 127) i retorna el nou valor.
     * <p>
     * [EN] Increases the velocity of the selected track by 10 units
     * (maximum 127) and returns the new value.
     *
     * @return [CA] nova velocitat de la pista / [EN] new track velocity
     */
    public int louder() {
        MyTrack tr = this.getCurrentTrack();
        int velocity = tr.getVelocity();
        velocity = Math.min(127, velocity + 10);
        tr.setVelocity(velocity);
        return velocity;
    }

    /**
     * [CA] Decreixenta la velocitat de la pista seleccionada en 10 unitats
     * (mínim 0) i retorna el nou valor.
     * <p>
     * [EN] Decreases the velocity of the selected track by 10 units
     * (minimum 0) and returns the new value.
     *
     * @return [CA] nova velocitat de la pista / [EN] new track velocity
     */
    public int quieter() {
        MyTrack tr = this.getCurrentTrack();
        int velocity = tr.getVelocity();
        velocity = Math.max(0, velocity - 10);
        tr.setVelocity(velocity);
        return velocity;
    }

    /**
     * [CA] Indica si el mesclador ha estat modificat des de l'última vegada que
     * es va desar o reiniciar el flag.
     * <p>
     * [EN] Returns whether the mixer has been modified since the last time
     * the flag was saved or reset.
     *
     * @return {@code true} si hi ha canvis pendents / {@code true} if there are pending changes
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * [CA] Estableix el flag de modificació del mesclador.
     * <p>
     * [EN] Sets the modification flag of the mixer.
     *
     * @param modified [CA] {@code true} si hi ha canvis / [EN] {@code true} if there are changes
     */
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * [CA] Retorna el canal MIDI actiu de la pista seleccionada actualment.
     * <p>
     * [EN] Returns the active MIDI channel of the currently selected track.
     *
     * @return [CA] índex del canal MIDI actiu / [EN] active MIDI channel index
     */
    public int getCurrentChannelOfCurrentTrack() {
        int index = currentTrack;
        MyTrack track = getTrackFromId(index);
        return track.getCurrentChannel();
    }

    /**
     * [CA] Retorna el canal MIDI actiu de la pista amb l'índex indicat.
     * <p>
     * [EN] Returns the active MIDI channel of the track at the given index.
     *
     * @param index [CA] identificador de la pista / [EN] track identifier
     * @return [CA] índex del canal MIDI actiu / [EN] active MIDI channel index
     */
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
    /**
     * [CA] Afegeix una pista al final de la llista de pistes.
     * <p>
     * [EN] Adds a track at the end of the track list.
     *
     * @param track [CA] pista a afegir / [EN] track to add
     */
    public void addTrack(MyTrack track) {
        addTrack(tracks.size(), track);
    }

    /**
     * [CA] Afegeix una pista a la posició indicada (ús intern).
     * <p>
     * [EN] Adds a track at the given position (internal use).
     *
     * @param pos   [CA] posició a la llista / [EN] position in the list
     * @param track [CA] pista a afegir / [EN] track to add
     */
    private void addTrack(int pos, MyTrack track) {
        if (!tracks.contains(track)) {
            tracks.add(pos, track);
        }
    }

    /**
     * [CA] Tanca la finestra del mesclador si és oberta i neteja la referència.
     * <p>
     * [EN] Closes the mixer window if it is open and clears the reference.
     */
    public void closeMixerWindow() {
        if (dialog != null) {
            dialog.dispose();
            dialog = null;
            mixerVisible = false;
        }
    }

    /**
     * [CA] Actualitza el contingut de la finestra del mesclador sense forçar
     * un redibuix complet de la partitura. Si el mesclador no és visible, no fa res.
     * S'assegura d'executar-se al EDT de Swing.
     * <p>
     * [EN] Updates the mixer window content without forcing a full score redraw.
     * If the mixer is not visible, does nothing. Ensures execution on the Swing EDT.
     */
    public void refreshMixer() {
        if (SwingUtilities.isEventDispatchThread()) {
            doRefreshMixer(false);
        } else {
            SwingUtilities.invokeLater(() -> doRefreshMixer(false));
        }
    }

    /**
     * [CA] Actualitza el contingut de la finestra del mesclador i força un redibuix
     * complet de tota la partitura (drawFullGridinOffscreen). S'assegura d'executar-se
     * al EDT de Swing.
     * <p>
     * [EN] Updates the mixer window content and forces a full score redraw
     * (drawFullGridinOffscreen). Ensures execution on the Swing EDT.
     */
    public void refreshMixerFull() {
        if (SwingUtilities.isEventDispatchThread()) {
            doRefreshMixer(true);
        } else {
            SwingUtilities.invokeLater(() -> doRefreshMixer(true));
        }
    }

    /**
     * [CA] Implementació interna del refresc del mesclador. Reconstrueix el panell
     * de pistes i redibuixa la partitura (complet o càmera actual).
     * <p>
     * [EN] Internal implementation of the mixer refresh. Rebuilds the track panel
     * and redraws the score (full or current camera).
     *
     * @param fullRedraw [CA] {@code true} per redibuixar tota la partitura /
     *                   [EN] {@code true} to redraw the entire score
     */
    private void doRefreshMixer(boolean fullRedraw) {
        if (this.isMixerVisible()) {
            rebuildTrackContent(true);
        }
        if (fullRedraw) {
            this.contr.getAllPurposeScore().drawFullGridinOffscreen();
        } else {
            this.contr.getAllPurposeScore().drawCurrentCamInOffscreen();
        }
        this.contr.getUi().getPanel().repinta(true);
    }

    /**
     * [CA] Reconstrueix el contingut del panell de pistes del diàleg del mesclador,
     * mostrant les pistes especials (acords, percussió) i les pistes normals amb notes.
     * <p>
     * [EN] Rebuilds the content of the mixer dialog's track panel, showing the special
     * tracks (chords, drums) and normal tracks with notes.
     *
     * @param doPack [CA] {@code true} per redimensionar el diàleg després de reconstruir /
     *               [EN] {@code true} to resize the dialog after rebuilding
     */
    private void rebuildTrackContent(boolean doPack) {
        if (dialog == null) return;
        contentPanel.removeAll();
        if (this.chordTrack != null && contr.getAllPurposeScore().hasAnyChords()) {
            addTrackToDialog(this.chordTrackId, true);
        }
        for (int i = 0; i < tracks.size(); i++) {
            MyTrack t = tracks.get(i);
            if (t == chordTrack || t == drumsTrack) continue; // ja mostrats com a tracks especials
            addTrackToDialog(i, false);
        }
        if (this.drumsTrack != null && this.drumsTrack.getnNotes() > 0) {
            addTrackToDialog(this.drumsTrackId, true);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
        if (doPack) dialog.pack();
    }

    /**
     * [CA] Comprova si ja existeix una pista amb el nom indicat (no distingeix majúscules).
     * <p>
     * [EN] Checks whether a track with the given name already exists (case-insensitive).
     *
     * @param nom [CA] nom a comprovar / [EN] name to check
     * @return {@code true} si ja existeix una pista amb aquest nom / {@code true} if a track with this name already exists
     */
    private boolean jaExisteixPistaAmbNom(String nom) {
        return tracks.stream().anyMatch(t -> t.getName().equalsIgnoreCase(nom));
    }

    /**
     * [CA] Obre la finestra del mesclador com a diàleg no-modal. Si ja hi havia
     * una finestra oberta, la tanca primer. La finestra inclou controls per mostrar,
     * silenciar, seleccionar, reanomenar, esborrar i afegir pistes, a més de
     * suport per a Ctrl+Z / Ctrl+Shift+Z.
     * <p>
     * [EN] Opens the mixer window as a non-modal dialog. If a window was already
     * open, it is closed first. The window includes controls to show, mute, select,
     * rename, delete and add tracks, plus support for Ctrl+Z / Ctrl+Shift+Z.
     */
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

// Afegim Key Binding per Ctrl+Z i Ctrl+Shift+Z
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

        rebuildTrackContent(false);

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

    /**
     * [CA] Afegeix la fila d'una pista al diàleg del mesclador. Mostra els botons
     * Show/Hide, Play/Mute, el label informatiu i el botó Select. Si la pista és
     * esborrada o buida (i no és nova ni s'ha forçat), no es mostra.
     * <p>
     * [EN] Adds a track row to the mixer dialog. Shows the Show/Hide, Play/Mute
     * buttons, the info label and the Select button. If the track is deleted or
     * empty (and not new or forced), it is not shown.
     *
     * @param index      [CA] identificador de la pista / [EN] track identifier
     * @param alwaysShow [CA] {@code true} per mostrar la pista fins i tot si és buida /
     *                   [EN] {@code true} to show the track even if empty
     */
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
            refreshMixerFull();
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

// Panell dreta: select
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton selectButton = new JButton(track.isSelected() ? "Selected" : "Select");
        selectButton.addActionListener(e -> {
            setCurrentTrack(index);
            refreshMixer();
            contr.redrawChordLine();
        });
        rightPanel.add(selectButton);

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

    /**
     * [CA] Actualitza el text d'un botó Show/Hide o Play/Mute d'acord amb l'estat
     * de visibilitat o audibilitat de la pista indicada.
     * <p>
     * [EN] Updates the text of a Show/Hide or Play/Mute button according to the
     * visibility or audibility state of the given track.
     *
     * @param button [CA] botó a actualitzar / [EN] button to update
     * @param index  [CA] identificador de la pista / [EN] track identifier
     * @param isShow [CA] {@code true} per actualitzar el botó Show/Hide, {@code false} per Play/Mute /
     *               [EN] {@code true} to update the Show/Hide button, {@code false} for Play/Mute
     */
    private void updateButtonState(JButton button, int index, boolean isShow) {
        MyTrack track = getTrackFromId(index);
        if (isShow) {
            boolean state = track.isVisible();
            button.setText(state ? "Show" : "Hide");
        } else {
            boolean state = track.isAudible();
            button.setText(state ? "Play" : "Mute");
        }
    }

    /**
     * [CA] Retorna la pista corresponent a l'identificador indicat. Per als ids
     * especials de la pista d'acords ({@link #chordTrackId}) i la de percussió
     * ({@link #drumsTrackId}), retorna les pistes especials. Per a la resta,
     * retorna la pista de la llista per índex.
     * <p>
     * [EN] Returns the track corresponding to the given identifier. For the special
     * chord track ({@link #chordTrackId}) and drums track ({@link #drumsTrackId}) ids,
     * returns the special tracks. For the rest, returns the track from the list by index.
     *
     * @param index [CA] identificador de la pista / [EN] track identifier
     * @return [CA] pista corresponent, o {@code null} si no existeix / [EN] corresponding track, or {@code null} if not found
     */
    public MyTrack getTrackFromId(int index){
        MyTrack track;
        if (index == this.getChordTrackId()){
            track = this.getChordTrack();
        } else if (index == this.getDrumsTrackId()){
            track = this.getDrumsTrack();
        } else {
            if (index < 0 || index >= tracks.size()) return null;
            track = tracks.get(index);
        }
        return track;
    }

    /**
     * [CA] Indica si la finestra del mesclador és visible.
     * <p>
     * [EN] Returns whether the mixer window is visible.
     *
     * @return {@code true} si el mesclador és visible / {@code true} if the mixer is visible
     */
    public boolean isMixerVisible() {
        return mixerVisible;
    }

    /**
     * [CA] Selecciona la pista amb l'identificador indicat com a pista activa.
     * Actualitza el flag de selecció de totes les pistes de la llista i de les
     * pistes especials d'acords i percussió.
     * <p>
     * [EN] Selects the track with the given identifier as the active track.
     * Updates the selection flag of all tracks in the list and of the special
     * chord and drums tracks.
     *
     * @param id [CA] identificador de la pista a seleccionar / [EN] identifier of the track to select
     */
    public void setCurrentTrack(int id) {
        this.currentTrack = id;
        for (int i = 0; i < tracks.size(); i++) {
            tracks.get(i).setSelected(i == id);
        }
        if (chordTrack != null) chordTrack.setSelected(id == this.getChordTrackId());
        if (drumsTrack != null) drumsTrack.setSelected(id == this.getDrumsTrackId());
    }

    /**
     * [CA] Retorna la pista seleccionada actualment. Retorna {@code null} si
     * no hi ha cap pista seleccionada (índex negatiu).
     * <p>
     * [EN] Returns the currently selected track. Returns {@code null} if
     * no track is selected (negative index).
     *
     * @return [CA] pista seleccionada, o {@code null} / [EN] selected track, or {@code null}
     */
    public MyTrack getCurrentTrack() {
        int index = currentTrack;
        if (index <0) return null;
        MyTrack track = getTrackFromId(index);
        return track;
    }

    /**
     * [CA] Retorna l'identificador numèric de la pista seleccionada actualment.
     * Retorna {@code -1} si no hi ha cap pista seleccionada.
     * <p>
     * [EN] Returns the numeric identifier of the currently selected track.
     * Returns {@code -1} if no track is selected.
     *
     * @return [CA] identificador de la pista seleccionada, o {@code -1} /
     *         [EN] selected track identifier, or {@code -1}
     */
    public int getCurrentTrackId() {
        return currentTrack;
    }

    /**
     * [CA] Retorna la llista completa de pistes del mesclador (sense les pistes
     * especials d'acords i percussió).
     * <p>
     * [EN] Returns the full list of tracks in the mixer (without the special
     * chord and drums tracks).
     *
     * @return [CA] llista de pistes / [EN] list of tracks
     */
    public List<MyTrack> getTracks() {
        return tracks;
    }

    /**
     * [CA] Marca la pista amb l'id indicat com a esborrada i reajusta la pista
     * seleccionada a l'última pista no esborrada. Reassigna els flags de selecció
     * a totes les pistes de la llista.
     * <p>
     * [EN] Marks the track with the given id as deleted and adjusts the selected
     * track to the last non-deleted track. Reassigns selection flags to all
     * tracks in the list.
     *
     * @param id [CA] identificador de la pista a esborrar / [EN] identifier of the track to delete
     */
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

    /**
     * [CA] Elimina de la llista els tracks buits (nNotes==0, no nous) carregats des de fitxer.
     * Després reassigna IDs i selecciona l'últim track vàlid.
     * <p>
     * [EN] Removes ghost tracks (nNotes==0, not new) loaded from file, then reassigns
     * IDs and selects the last valid track.
     */
    public void removeEmptyTracks() {
        for (MyTrack t : tracks) {
            if (t.getnNotes() <= 0 && !t.isIsNew()) {
                t.setDeleted(true);
                t.setVisible(false);
            }
        }
        int n = tracks.size() - 1;
        while (n >= 0 && tracks.get(n).isDeleted()) n--;
        currentTrack = n;
        for (int i = 0; i < tracks.size(); i++) {
            tracks.get(i).setSelected(i == currentTrack);
        }
    }

}
