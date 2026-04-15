package dodecagraphone.model.mixer;

import dodecagraphone.model.sound.SoundWithMidi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyTrack {
    private int id;
    private String nomPista;
    private List<Integer> canals;
    private int currentChannel;
    private boolean dotted;
    private long nNotes;
    private int velocity;
    private boolean keepNoteVelocity;
    private boolean selected; // Nou camp per indicar si és la pista seleccionada
    private boolean deleted;
    private boolean visible;
    private boolean audible;
    private boolean isNew = false;

    public MyTrack(int id, String nomPista) {
        this.id = id;
        this.nomPista = nomPista;
        this.canals = new ArrayList<>();
        this.currentChannel = -1;
        this.dotted = false;
        this.nNotes = 0;
        this.velocity = 127;
        this.keepNoteVelocity = false;
        this.selected = false;
        this.deleted = false;
        this.visible = true;
        this.audible = true;
        this.isNew = false;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isAudible() {
        return audible;
    }

    public void setAudible(boolean audible) {
        this.audible = audible;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.nNotes = 0;
        this.deleted = deleted;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isKeepNoteVelocity() {
        return keepNoteVelocity;
    }

    public void setKeepNoteVelocity(boolean keepNoteVelocity) {
        this.keepNoteVelocity = keepNoteVelocity;
    }

    public boolean isDotted() {
        return dotted;
    }

    public void setDotted(boolean dotted) {
        this.dotted = dotted;
    }

    public boolean isIsNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public long getnNotes() {
        return nNotes;
    }

    public void oneNoteMore() {
        this.isNew = false;
        this.nNotes++;
    }

    public void oneNoteLess() {
        this.nNotes--;
        if (this.nNotes == 0) isNew = true;
    }

    public boolean isEmpty() {
        return this.nNotes <= 0;
    }

    public void setnNotes(long nNotes) {
        this.nNotes = nNotes;
    }

    public void toggleDotted() {
        this.dotted = !this.dotted;
        System.out.println("MyTrack::toggleDotted(): " + this.getName() + " " + this.dotted);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return nomPista;
    }

    public void setName(String nomPista) {
        this.nomPista = nomPista;
    }

    public List<Integer> getCanals() {
        return canals;
    }

    public String toStringCanalsInstruments() {
        Map<Integer, String> canalsInstrs = toMapCanalsInstruments();
        StringBuilder cadena = new StringBuilder("{");
        int i = 0;
        for (int canal : canalsInstrs.keySet()) {
            if (canal == this.currentChannel) cadena.append(">>");
            cadena.append(canal).append(":").append(canalsInstrs.get(canal));
            if (i < canalsInstrs.size() - 1) cadena.append(", ");
            i++;
        }
        cadena.append("}");
        return cadena.toString();
    }

    public Map<Integer, String> toMapCanalsInstruments() {
        HashMap<Integer, String> canalsInstrs = new HashMap<>();
        for (int chan : this.canals) {
            canalsInstrs.put(chan, SoundWithMidi.getInstrumentMnemonic(SoundWithMidi.getInstrumentInChannel(chan)));
        }
        return canalsInstrs;
    }

    public final void afegirCanal(int canal) {
        if (!canals.contains(canal)) canals.add(canal);
    }

    public int getCurrentChannel() {
        return currentChannel;
    }

    public final void setCurrentChannel(int currentChannel) {
        this.currentChannel = currentChannel;
    }
} 
