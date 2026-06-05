/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.ui;

import dodecagraphone.MyController;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * [CA] Finestra principal de l'aplicació (JFrame). Gestiona el cicle de vida
 * de la finestra: maximització inicial, reposicionament quan es restaura des
 * del mode maximitzat, i confirmació de sortida amb possible desat de la
 * partitura. Conté el {@link MyNewPanel} (zona de dibuix) i el
 * {@link MyController} (lògica de negoci).
 * <p>
 * [EN] Main application window (JFrame). Manages the window lifecycle:
 * initial maximisation, repositioning when restored from maximised state, and
 * exit confirmation with optional score saving. Contains the
 * {@link MyNewPanel} (drawing area) and {@link MyController} (business logic).
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyUserInterface extends JFrame {
    private final MyController controller;
    private MyNewPanel panel;
    private String version;

    /**
     * [CA] Crea la finestra principal, inicialitza el controlador i el panell
     * de dibuix, estableix la mida màxima dins de l'àrea de treball i maximitza
     * la finestra. Registra els listeners de canvi d'estat i de tancament.
     * <p>
     * [EN] Creates the main window, initialises the controller and drawing
     * panel, constrains the initial size to the work area and maximises the
     * window. Registers window state and closing listeners.
     */
    public MyUserInterface() {
        super("DoDeReBu_App_v4.0");
        this.version = this.getTitle();
        this.controller = new MyController(this);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        panel = new MyNewPanel(this.controller);
        panel.setDoubleBuffered(false);
        panel.setIgnoreRepaint(true);
        this.add(panel);
        this.pack();

        // Limita la mida inicial (restored bounds) al work area (pantalla - taskbar)
        // perquè quan l'usuari surti del full-screen no quedi amagat darrere la taskbar.
        // setBounds() s'ha de cridar ABANS de setExtendedState perquè Windows recordi
        // les "restored bounds" correctes.
        Rectangle workArea = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        Rectangle bounds = getBounds();
        if (bounds.width  > workArea.width)  bounds.width  = workArea.width;
        if (bounds.height > workArea.height) bounds.height = workArea.height;
        if (bounds.x < workArea.x) bounds.x = workArea.x;
        if (bounds.y < workArea.y) bounds.y = workArea.y;
        if (bounds.x + bounds.width  > workArea.x + workArea.width)
            bounds.x = workArea.x + workArea.width  - bounds.width;
        if (bounds.y + bounds.height > workArea.y + workArea.height)
            bounds.y = workArea.y + workArea.height - bounds.height;
        setBounds(bounds);

        // Inicialitzem Settings amb les dimensions reals del panel maximitzat
        // ABANS de setVisible, perquè el primer paint ja trobi els buffers
        // offscreen al tamany correcte i no hi hagi flash de tamany petit.
        // Els insets (títol + vores) ja els coneixem del pack().
        java.awt.Insets insets = getInsets();
        int panelW = workArea.width  - insets.left - insets.right;
        int panelH = workArea.height - insets.top  - insets.bottom;
        if (panelW > 0 && panelH > 0) {
            Settings.setScreenPixelDimensions(panelW, panelH);
            controller.onScreenResized();
        }

        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setVisible(true);

        // Correcció final amb les dimensions reals del panel (per si els insets
        // estimats diferien lleugerament de les dimensions definitives).
        SwingUtilities.invokeLater(() -> {
            int w = panel.getWidth();
            int h = panel.getHeight();
            if (w > 0 && h > 0 && (w != panelW || h != panelH)) {
                Settings.setScreenPixelDimensions(w, h);
                controller.onScreenResized();
            }
        });

        // Quan la finestra passa de maximitzada a normal, assegura que no quedi
        // amagada darrere la barra de tasques (per exemple si les restored bounds
        // sobrepassaven el work area).
        this.addWindowStateListener(e -> {
            boolean wasMaximized = (e.getOldState() & Frame.MAXIMIZED_BOTH) != 0;
            boolean isNormal     = (e.getNewState() & Frame.MAXIMIZED_BOTH) == 0;
            if (wasMaximized && isNormal) {
                SwingUtilities.invokeLater(() -> {
                    Rectangle wa = GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getMaximumWindowBounds();
                    Rectangle b  = getBounds();
                    boolean changed = false;
                    if (b.x < wa.x) { b.x = wa.x; changed = true; }
                    if (b.y < wa.y) { b.y = wa.y; changed = true; }
                    if (b.width  > wa.width)  { b.width  = wa.width;  changed = true; }
                    if (b.height > wa.height) { b.height = wa.height; changed = true; }
                    if (b.x + b.width  > wa.x + wa.width)
                        { b.x = wa.x + wa.width  - b.width;  changed = true; }
                    if (b.y + b.height > wa.y + wa.height)
                        { b.y = wa.y + wa.height - b.height; changed = true; }
                    if (changed) setBounds(b);
                });
            }
        });

        // Executar onExitCheckNSave() quan es tanqui la finestra
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (controller.onExitCheckNSave()) {
                    e.getWindow().dispose(); // només tanquem si l'usuari no ha cancel·lat
                    System.exit(0);
                }
            }
        });
    }

    /**
     * [CA] Retorna la cadena de versió de l'aplicació (títol de la finestra).
     * <p>
     * [EN] Returns the application version string (window title).
     *
     * @return [CA] versió de l'aplicació / [EN] application version string
     */
    public String getVersion() {
        return version;
    }

    /**
     * [CA] Retorna el controlador principal de l'aplicació.
     * <p>
     * [EN] Returns the main application controller.
     *
     * @return [CA] controlador principal / [EN] main controller
     */
    public MyController getController(){
        return this.controller;
    }

    /**
     * [CA] Retorna el panell principal de dibuix.
     * <p>
     * [EN] Returns the main drawing panel.
     *
     * @return [CA] panell de dibuix / [EN] drawing panel
     */
    public MyNewPanel getPanel() {
        return panel;
    }

    /**
     * [CA] Invocada pel bucle principal (Timer). Actualitza l'estat del
     * controlador i, si cal, repinta el panell.
     * <p>
     * [EN] Invoked by the main loop (Timer). Updates the controller state
     * and repaints the panel if needed.
     */
    public void update() {
//            this.controller.update();
//            this.panel.repinta(true);
//
        if (this.controller.update() && !this.controller.getCam().isPlaying()) {
            this.panel.repinta(true);
        }
//        if (this.controller.update()) {
////            MyController.setDoRedraw(true);
////            try {
////                SwingUtilities.invokeAndWait(() -> this.repaint());
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
//            //this.panel.paintImmediately(this.panel.getBounds());
//            //this.validate();
//            System.out.println("MyUserInterface::update: controller.update is true, repaint is now called");
//            this.controller.setNeedsDrawing(true);
//            this.repaint(); // activa panel.paintComponent() ergo Controller.redraw()
//            //this.controller.getMixer().repaintMixer();
//        }
    }

}

///**
// * Manages drawing (paintComponent()), mouse and buttons. Drawing is done on the
// * JPanel (drawing on an Image of the JFrame produces flickering).
// *
// * @author upcnet
// */
//public class MyNewPanel extends JPanel implements ActionListener, KeyListener {
//
//    private MyController controller;
//    private JButton b, b2;
////    private Image buffer; // Imatge en memòria
////    private Graphics bufferGraphics; // Objecte Graphics per a la imatge en memòria
//
//    public MyNewPanel(MyController contr) {
// //               setPreferredSize(new Dimension(500, 500));
////        Dimension d = getPreferredSize();
////        buffer = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
////        bufferGraphics = buffer.getGraphics();
//
//        this.controller = contr;
//        this.setDoubleBuffered(true);        // Dibuixa els gràfics a la imatge en memòria
//        this.setBorder(BorderFactory.createLineBorder(Color.black));
//        this.addMouseListener(new MyNewMouseAdapter(this, this.controller));
//        this.addMouseMotionListener(new MyNewMouseAdapter(this, this.controller));
//
//        // ImageIcon leftButtonIcon = createImageIcon("images/right.gif");
//        this.b = new JButton("Test Button");//,leftButtonIcon);
////        b1.setVerticalTextPosition(AbstractButton.CENTER);
////        b1.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
////        b1.setMnemonic(KeyEvent.VK_D);
//        this.b.setActionCommand("button");
//
//        //Listen for actions on buttons 1 and 3.
//        b.addActionListener(this);
//        b.setToolTipText("Click this button to show demo message.");
//
//        //Add Components to this container, using the default FlowLayout.
////        this.add(b);
//        this.b2 = new JButton("Test Button");
//        this.b2.setActionCommand("button2");
//        b2.addActionListener(this);
//        b2.setToolTipText("Click this button to show demo 2 message.");
////        this.add(b2);
//        this.addKeyListener(this);
//    }
//
//    @Override
//    public Dimension getPreferredSize() {
//        return new Dimension((int)Settings.getScreenWidth(),(int)Settings.getScreenHeight());
//    }
//
//    /**
//     * The key listener requires the focus on the JPanel.
//     */
//    @Override
//    public void addNotify() {
//        super.addNotify();
//        requestFocus();
//    }
//
//    private static int count = 0;
//    /**
//     * Main paint method. It is automatically invoked at each window change and
//     * when repaint() is called (either on the JFrame or the JPanel). It should
//     * not be explicitly called.
//     *
//     * @param g
//     */
//    @Override
//    public void paintComponent(Graphics g) {
//        super.paintComponent(g);
////        draw(bufferGraphics);
////        if (count%2==1) {
//            count ++;
////        if (this.controller.isNeedsDrawing()) {
////            MyController.setDoRedraw(true);
////            try {
////                SwingUtilities.invokeAndWait(() -> this.repaint());
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
//            //this.panel.paintImmediately(this.panel.getBounds());
//            //this.validate();
////            System.out.println("MyUserInterface::paintComponent: redraw is now called");
//            this.controller.redraw((Graphics2D) g);
//            //this.controller.setNeedsDrawing(false);
////            this.repaint(); // activa panel.paintComponent() ergo Controller.redraw()
//            //this.controller.getMixer().repaintMixer();
////        }
////            count = 0;
////        }
////        System.out.println("MyUserInterface::paintComponent: count = "+ count);
//
////        // Copia la imatge en memòria a la pantalla
////        g.drawImage(buffer, 0, 0, null);
//    }
//
////    private void draw(Graphics g) {
////        // Aquí pots posar el teu codi per dibuixar els gràfics
////    }
////
//    /**
//     * Action performed, triggered by a button event.
//     *
//     * @param e
//     */
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        if ("button".equals(e.getActionCommand())) {
//            this.controller.onDemoButtonPressed();
//            //        b.setEnabled(false);
//        } else if ("button2".equals(e.getActionCommand())) {
//            this.controller.onDemoButton2Pressed();
//            //        b.setEnabled(false);
//        }
//    }
//
////    /** Returns an ImageIcon, or null if the path was invalid. */
////    protected static ImageIcon createImageIcon(String path) {
////        java.net.URL imgURL = ButtonDemo.class.getResource(path);
////        if (imgURL != null) {
////            return new ImageIcon(imgURL);
////        } else {
////            System.err.println("Couldn't find file: " + path);
////            return null;
////        }
////    }
//    @Override
//    public void keyTyped(KeyEvent e) {
////        System.err.println("Key typed, not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void keyPressed(KeyEvent e) {
//        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
//            if (e.isShiftDown()) {
//                controller.redo();
//            } else {
////                System.out.println("Undo");
//                controller.undo();
//            }
//            this.repaint();
//        }
//    }
//
//    @Override
//    public void keyReleased(KeyEvent e) {
//        // System.err.println("Key released."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//}
//
///**
// * Overrides callback methods invoked by mouse events.
// *
// * @author upcnet
// */
//class MyNewMouseAdapter extends MouseAdapter {
//
//    MyController controller;
//    JPanel panel;
//
//    public MyNewMouseAdapter(JPanel panel, MyController contr) {
//        super();
//        this.panel = panel;
//        this.controller = contr;
//    }
//
//    @Override
//    public void mouseMoved(MouseEvent e){
//        double posX = e.getX();
//        double posY = e.getY();
//        controller.onMouseMoved(posX, posY);
//        // this.panel.repaint();
//    }
//
//    @Override
//    public void mouseDragged(MouseEvent e) {
////        System.err.println("MyMouseListener.mouseDragged()");
//        double posX = e.getX();
//        double posY = e.getY();
//        controller.onMouseDragged(posX, posY);
//        this.panel.repaint();
//    }
//
//    @Override
//    public void mousePressed(MouseEvent e) {
////        System.err.println("MyMouseListener.mousePressed()");
//        int pointer = e.getButton();
//        if (pointer == 1) {
//            double posX = e.getX();
//            double posY = e.getY();
//            this.controller.onMousePressed(posX, posY);
//            this.panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
//            this.panel.repaint();
//        }
//    }
//
//    @Override
//    public void mouseReleased(MouseEvent e) {
//        int pointer = e.getButton();
//        if (pointer == 1) {
//            double posX = e.getX();
//            double posY = e.getY();
//            this.controller.onMouseReleased(posX, posY);
//            this.panel.repaint();
//        }
//    }
//}
