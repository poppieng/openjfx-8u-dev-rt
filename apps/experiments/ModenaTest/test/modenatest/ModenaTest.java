/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package modenatest;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import modena.Modena;
import org.jemmy.Rectangle;
import org.jemmy.control.Wrap;
import org.jemmy.env.Environment;
import org.jemmy.env.Timeout;
import org.jemmy.fx.NodeDock;
import org.jemmy.image.AWTImage;
import org.jemmy.image.FilesystemImageLoader;
import org.jemmy.image.Image;
import org.jemmy.image.ImageCapturer;
import org.jemmy.image.ImageLoader;
import org.jemmy.image.StrictImageComparator;
import org.jemmy.timing.State;
import org.jemmy.timing.Waiter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static modenatest.ModenaTest.Configuration.*;
import org.jemmy.action.GetAction;
import org.jemmy.fx.QueueExecutor;


/**
 * This test runs Modena app and waits for snapshots obtained from the app content
 * to match the golden files.
 *
 * Unless they perfectly match test will fail. The following information is collected
 * for failed comparisons:
 * current snapshot in build/test/results folder
 * observed image difference in build/test/results/diffs folder
 *
 * Not all failures necessary mean JavaFX is broken. Here are possible exceptions:
 * - Animated content can't be compared exactly using this approach.
 * - Some minor variations in layout/colors are possible.
 * Manual image comparison is required for all such cases. If observed image is
 * correct you could copy it to corresponding golden images folder. Golden images
 * are located in golden/seven, golden/xp, golden/mac or golden/linux folders.
 *
 * This project requires Jemmy libraries which are downloaded from SQE repository
 * on first run (see build.xml or lib/readme.txt)
 *
 * @author akouznet
 */
@RunWith(Parameterized.class)
public class ModenaTest {

    private static File osFolder;
    private static File goldenFolder;
    private static File resFolder;
    private static File diffFolder;
    private static ImageLoader imageLoader;
    private static final String DEFAULT_OS_FOLDER_NAME = "seven";
    private static Configuration[] configurations;

    /*
     * The following method copied from
     * SharedTestUtils\src\test\javaclient\shared\screenshots\ImagesManager.java
     */
    private static String osFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        String version = System.getProperty("os.version").toLowerCase();
        if (os.indexOf("win") >= 0) {
            configurations = new Configuration[] { DEFAULT, ALTERED_COLORS, WINDOWS_125, WINDOWS_150 };
            if (version.startsWith("5.")) {
                return "xp";
            } else if (version.startsWith("6.")) {
                return "seven";
            }
        } else if (os.indexOf("mac") >= 0) {
            if (isRetina()) {
                configurations = new Configuration[] { DEFAULT_RETINA, ALTERED_COLORS_RETINA };
            } else {
                configurations = new Configuration[] { DEFAULT, ALTERED_COLORS };
            }
            return "mac";
        } else if ((os.indexOf("linux") >= 0) || (os.indexOf("ubuntu") >= 0)) {
            configurations = new Configuration[] { DEFAULT, ALTERED_COLORS };
            return "linux";
        }
        configurations = new Configuration[] { DEFAULT, ALTERED_COLORS };
        return DEFAULT_OS_FOLDER_NAME;
    }

    private static boolean isRetina() {
        return false; // TODO: Fix this
    }

    @Parameterized.Parameters
    public static List<Object[]> imageNames() {


        osFolder = new File(new File("golden"), osFolder());
        System.out.println("Golden base folder = " + osFolder.getAbsolutePath());

        imageLoader = new FilesystemImageLoader();

        new Thread(new Runnable() {

            @Override
            public void run() {
                Modena.launch(Modena.class, Modena.TEST);
                System.out.println("Modena UI launched");
            }
        }).start();

        AWTImage.setComparator(new StrictImageComparator());
        Environment.getEnvironment().setExecutor(QueueExecutor.EXECUTOR);
        Environment.getEnvironment().setImageCapturer(new ImageCapturer() {

            @Override
            public Image capture(final Wrap<?> wrap, Rectangle rctngl) {
                final Node node = (Node) wrap.getControl();
                return new AWTImage(SwingFXUtils.fromFXImage(new GetAction<WritableImage>() {

                    @Override
                    public void run(Object... parameters) throws Exception {
                        setResult(node.snapshot(null, null));
                    }

                    @Override
                    public String toString() {
                        return "Obtaining snapshot for " + wrap;
                    }
                }.dispatch(Environment.getEnvironment()), null));
            }
        });

        Map<String, Node> content = new Waiter(new Timeout(null, 60_000))
                .ensureState(new State<Modena>() {

            @Override
            public String toString() {
                return "Waiting for Modena instance";
            }

            @Override
            public Modena reached() {
                return Modena.getInstance();
            }
        }).getContent();

        ArrayList<Object[]> arrayList = new ArrayList<>(content.keySet().size() * configurations.length);
        for (Configuration configuration : configurations) {
            for (String name : content.keySet()) {
                arrayList.add(new Object[] { name, configuration });
            }
        }
        return arrayList;
    }

    public static enum Configuration {

        DEFAULT("default", null, 13),
        ALTERED_COLORS("altered_colors", null, 13, false, true),
        ALTERED_COLORS_RETINA("altered_colors_retina", null, 13, true, true),
        DEFAULT_RETINA("retina", null, 13, true, false),
        WINDOWS_125("125", "Segoe UI", 15),
        WINDOWS_150("150", "Segoe UI", 18),
        EMBEDDED_TOUCH("touch", "Arial", 22),
        ENBEDDED_SMALL("small", "Arial", 9);
//        MAC("13px", "Lucida Grande", 13),
//        WINDOWS_100("100", "Segoe UI", 12),

        public String folderName;
        public String fontName;
        public int fontSize;
        public boolean defaultColors;
        public boolean retinaMode;

        private Configuration(String folderName, String fontName, int fontSize) {
            this(folderName, fontName, fontSize, true);
        }

        private Configuration(String folderName, String fontName, int fontSize, boolean defaultColors) {
            this(folderName, fontName, fontSize, defaultColors, false);
        }

        private Configuration(String folderName, String fontName, int fontSize, boolean defaultColors, boolean retinaMode) {
            this.folderName = folderName;
            this.fontName = fontName;
            this.fontSize = fontSize;
            this.defaultColors = defaultColors;
            this.retinaMode = retinaMode;
        }
    }

    private String imageName;
    private Configuration configuration;

    private static Configuration currentConfiguration;

    public ModenaTest(String imageName, Configuration configuration) {
        this.imageName = imageName;
        this.configuration = configuration;
    }

    @Before
    public void setUp() {
        if (configuration != currentConfiguration) {
            System.out.println("Setting up for new configuration " + configuration);
            goldenFolder = new File(osFolder, configuration.folderName);
            File buildTest = new File(new File("build"), "test");
            resFolder = new File(new File(buildTest, "results"), configuration.folderName);
            diffFolder = new File(resFolder, "diffs");
            resFolder.mkdirs();

            System.out.println("Golden folder = " + goldenFolder.getAbsolutePath());
            System.out.println("Diffs  folder = " + diffFolder.getAbsolutePath());
            System.out.println("Result folder = " + resFolder.getAbsolutePath());

            new GetAction<Void>() {

                @Override
                public void run(Object... parameters) throws Exception {
                    Modena modena = Modena.getInstance();
                    if (currentConfiguration != null) {
                        System.out.println("Restarting Modena UI");
                        modena.restart();
                    }
                    currentConfiguration = configuration;
                    if (configuration.fontName != null) {
                        Modena.getInstance().setFont(configuration.fontName, configuration.fontSize);
                    }
                    if (!configuration.defaultColors) {
                        Modena.getInstance().setAccentColor(Color.CRIMSON);
                        Modena.getInstance().setBaseColor(Color.LIGHTGREEN);
                    }
                }

                @Override
                public String toString() {
                    return "Setting up Modena UI for " + configuration;
                }
            }.dispatch(Environment.getEnvironment());
        }
    }

    @Test
    public void testNodeImage() {
        String filenameBase = imageName.replaceAll("[^A-Za-z_0-9]+", " ").trim();
        String goldenFilename = filenameBase + ".png";
        String resultFilename = filenameBase + ".png";
        String diffFilename = filenameBase + "_diff.png";
        Node node = Modena.getInstance().getContent().get(imageName);
        NodeDock nodeDock = new NodeDock(Environment.getEnvironment(), node);
        File goldenFile = new File(goldenFolder, goldenFilename);
        File resFile = new File(resFolder, resultFilename);
        File diffFile = new File(diffFolder, diffFilename);
        if (goldenFile.exists()) {
            try {
                nodeDock.wrap().waitImage(imageLoader.load(goldenFile.getAbsolutePath()),
                        resFile.getAbsolutePath(),
                        diffFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace(System.err);
//                try {
//                    AnimatedGIFCreator.createImage(new File(diffFolder, filenameBase + ".gif"),
//                            ((AWTImage) imageLoader.load(goldenFile.getAbsolutePath())).getTheImage(),
//                            ((AWTImage) imageLoader.load(resFile.getAbsolutePath())).getTheImage());
//                } catch (IOException ex) {
//                    Logger.getLogger(ModenaTest.class.getName()).
//                            log(Level.SEVERE, null, ex);
//                }
                throw new AssertionError(filenameBase + " image doesn't match golden in "
                        + configuration.folderName, e);
            }
        } else {
            nodeDock.wrap().getScreenImage().save(resFile.getAbsolutePath());
            throw new AssertionError("No golden file for " + filenameBase + " in "
                    + configuration.folderName);
        }
    }
}
