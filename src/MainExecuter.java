import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.TextAction;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainExecuter
{
    static final String APP_NAME = "Text-Inverter ";
    static final String APP_VERSION = "200718C";
    static final String APP_NONAME_FILE = "Noname.Txt";

    static class MainMenuBar
    {
        private final JMenuBar m_mbMain = new JMenuBar();
        private final JPopupMenu m_pmMain = new JPopupMenu();
        private final ActionListener m_alHandler;

        public MainMenuBar(ActionListener alListener)
        {
            m_alHandler = alListener;

            /* FILE */
            JMenu mnuFile = new JMenu("File");

            mnuFile.add(getMenuItem("New", 'N'));
            mnuFile.addSeparator();
            mnuFile.add(getMenuItem("Open", 'O'));
            mnuFile.add(getMenuItem("Save", 'S'));
            m_pmMain.add(getMenuItem("Save", 'S'));
            mnuFile.add(getMenuItem("Save As.."));
            mnuFile.addSeparator();
            mnuFile.add(getMenuItem("Exit", 'X'));

            m_mbMain.add(mnuFile);
            m_pmMain.addSeparator();
            /*--------------------------------------------------------------------------------------------------------*/

            /* EDIT */
            JMenu mnuEdit = new JMenu("Edit");

            mnuEdit.add(getActionMenuItem(new DefaultEditorKit.CutAction(), "Cut", 'X'));
            m_pmMain.add(getActionMenuItem(new DefaultEditorKit.CutAction(), "Cut", 'X'));
            mnuEdit.add(getActionMenuItem(new DefaultEditorKit.CopyAction(),"Copy", 'C'));
            m_pmMain.add(getActionMenuItem(new DefaultEditorKit.CopyAction(),"Copy", 'C'));
            mnuEdit.add(getActionMenuItem(new DefaultEditorKit.PasteAction(), "Paste", 'V'));
            m_pmMain.add(getActionMenuItem(new DefaultEditorKit.PasteAction(), "Paste", 'V'));

            m_mbMain.add(mnuEdit);
            m_pmMain.addSeparator();
            /*--------------------------------------------------------------------------------------------------------*/

            /* Script */
            JMenu mnuScript = new JMenu("Script");

            mnuScript.add(getMenuItem("DO INVERT!"));
            m_pmMain.add(getMenuItem("DO INVERT!"));
            mnuScript.addSeparator();
            mnuScript.add(getMenuItem("List", 'L'));
            mnuScript.add(getMenuItem("Export To File", 'E'));
            mnuScript.add(getMenuItem("Import From File", 'M'));

            m_pmMain.add(mnuScript);
            m_mbMain.add(mnuScript);
            /*--------------------------------------------------------------------------------------------------------*/

            /* Help */
            JMenu mnuHelp = new JMenu("Help");

            mnuHelp.add(getMenuItem("About.."));

            m_mbMain.add(mnuHelp);
            /*--------------------------------------------------------------------------------------------------------*/
        }

        public JMenuBar getMenuBar()
        {
            return m_mbMain;
        }

        public JPopupMenu getPopupBar()
        {
            return m_pmMain;
        }

        private JMenuItem getActionMenuItem(TextAction taParam, String strCaption, Character cchKeyCode)
        {
            JMenuItem mnuTemp;

            taParam.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(cchKeyCode, InputEvent.CTRL_DOWN_MASK));
            taParam.putValue(AbstractAction.NAME, strCaption);

            mnuTemp = new JMenuItem(taParam);
            return mnuTemp;
        }

        private JMenuItem getMenuItem(String strCaption)
        {
            return getMenuItem(strCaption, null);
        }

        private JMenuItem getMenuItem(String strCaption, Character cchKeyCode)
        {
            JMenuItem mnuTemp = new JMenuItem(strCaption);

            if (cchKeyCode != null)
            {
                mnuTemp.setAccelerator(KeyStroke.getKeyStroke(cchKeyCode, InputEvent.CTRL_DOWN_MASK));
            }

            mnuTemp.addActionListener(m_alHandler);
            return mnuTemp;
        }
    }

    private static final Dimension m_dScreenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private static MainFrame m_mfInstance = null;
    private static ScriptFrame m_sfSetting = null;
    private static JSONArray m_jaScripts = null;
    private static String m_strCurrentFile = null;

    private static void initScriptWindow()
    {
        if (m_sfSetting == null)
        {
            m_sfSetting = new ScriptFrame();

            m_sfSetting.setDefault();
            m_sfSetting.setSize(400, 400);
            m_sfSetting.setLocation(m_mfInstance.getX() + (m_sfSetting.getWidth() / 2), m_mfInstance.getY() + (m_sfSetting.getHeight() / 3));
        }
    }

    private static String showFileDialog()
    {
        return showFileDialog(true, null);
    }

    private static String showFileDialog(boolean isSave)
    {
        return showFileDialog(isSave, null);
    }

    private static String showFileDialog(String strDefault)
    {
        return showFileDialog(true, strDefault);
    }

    private static String showFileDialog(boolean isSave, String strDefault)
    {
        FileDialog fdInstance = new FileDialog(m_mfInstance, "Choose File", isSave ? FileDialog.SAVE : FileDialog.LOAD);

        if (strDefault != null)
        {
            fdInstance.setFile(strDefault);
        }

        fdInstance.setDirectory(System.getProperty("user.home"));
        fdInstance.setVisible(true);
        if (fdInstance.getFile() != null)
        {
            return fdInstance.getDirectory() + fdInstance.getFile();
        }
        else
        {
            return null;
        }
    }

    private static final String convertSpecialString(String strParam)
    {
        String strResult = strParam;

        //strResult = strResult.replaceAll("[*]", "\\\\[*]");
        strResult = strResult.replaceAll("[+]", "\\\\[+]");
        strResult = strResult.replaceAll("[$]", "\\\\[$]");
        strResult = strResult.replaceAll("[|]", "\\\\[|]");

        strResult = strResult.replaceAll("\\*", "\\\\*");
        strResult = strResult.replaceAll("\\^", "\\\\^");
        strResult = strResult.replaceAll("\\(", "\\\\C");
        strResult = strResult.replaceAll("\\)", "\\\\)");
        strResult = strResult.replaceAll("\\{", "\\\\{");
        strResult = strResult.replaceAll("\\[", "\\\\[");

        return strResult;
    }

    private static final ActionListener m_alEventHandler = evt ->
    {
        if (m_jaScripts == null)
        {
            m_jaScripts = new JSONArray();
        }

        if (evt.getActionCommand().contains("New"))
        {
            int iResult = JOptionPane.showConfirmDialog(m_mfInstance,
                    "Would You Like to Create a New File?", "Caution! No Save Yet!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (iResult == JOptionPane.YES_OPTION)
            {
                m_mfInstance.setText("");
            }
        }
        else if (evt.getActionCommand().contains("Open"))
        {
            try
            {
                String strPath = showFileDialog(false);

                if (strPath != null)
                {
                    FileInputStream fisTemp = new FileInputStream(strPath);

                    m_strCurrentFile = strPath;
                    m_mfInstance.setText(new String(fisTemp.readAllBytes()));
                    fisTemp.close();
                }
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(m_mfInstance, "Failed to Import the Scripts.", "ERROR!", JOptionPane.WARNING_MESSAGE);
                e.printStackTrace();
            }
        }
        else if (evt.getActionCommand().contains("Save As"))
        {
            String strPath;

            strPath = showFileDialog();
            if (strPath != null)
            {
                m_strCurrentFile = strPath;
            }
            else
            {
                return;
            }

            try
            {
                FileOutputStream fioTemp = new FileOutputStream(m_strCurrentFile);

                fioTemp.write(m_mfInstance.getText().getBytes());
                fioTemp.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(m_mfInstance, "Failed to Import the Scripts.", "ERROR!", JOptionPane.WARNING_MESSAGE);
                e.printStackTrace();
            }
        }
        else if (evt.getActionCommand().contains("Save"))
        {
            String strPath;

            if (m_strCurrentFile == null)
            {
                strPath = showFileDialog(APP_NONAME_FILE);
                if (strPath == null)
                {
                    return;
                }

                m_strCurrentFile = strPath;
            }

            try
            {
                FileOutputStream fioTemp = new FileOutputStream(m_strCurrentFile);

                fioTemp.write(m_mfInstance.getText().getBytes());
                fioTemp.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(m_mfInstance, "Failed to Import the Scripts.", "ERROR!", JOptionPane.WARNING_MESSAGE);
                e.printStackTrace();
            }
        }
        else if (evt.getActionCommand().contains("Export"))
        {
            try
            {
                if (m_jaScripts.length() > 0)
                {
                    FileDialog fdInstance  = new FileDialog(m_mfInstance, "Choose File For Exporting", FileDialog.SAVE);

                    fdInstance.setDirectory(System.getProperty("user.home"));
                    fdInstance.setVisible(true);

                    if (fdInstance.getFile() != null)
                    {
                        FileOutputStream fioTemp = new FileOutputStream(fdInstance.getDirectory() + fdInstance.getFile());
                        fioTemp.write(m_jaScripts.toString().getBytes());
                        fioTemp.close();
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(m_mfInstance, "There're No Script Items.", "ERROR!", JOptionPane.WARNING_MESSAGE);
                }
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(m_mfInstance, "Failed to Export the Scripts.", "ERROR!", JOptionPane.WARNING_MESSAGE);
                e.printStackTrace();
            }
        }
        else if (evt.getActionCommand().contains("Import"))
        {
            FileDialog fdInstance = new FileDialog(m_mfInstance, "Choose File For Importing", FileDialog.LOAD);

            fdInstance.setDirectory(System.getProperty("user.home"));
            fdInstance.setVisible(true);

            try
            {
                if (fdInstance.getFile() != null)
                {
                    FileInputStream fisTemp = new FileInputStream(fdInstance.getDirectory() + fdInstance.getFile());

                    m_jaScripts = new JSONArray(new String(fisTemp.readAllBytes()));
                    fisTemp.close();

                    initScriptWindow();

                    m_sfSetting.setDefaultList(m_jaScripts);
                    m_sfSetting.setVisible(true);
                }
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(m_mfInstance, "Failed to Import the Scripts.", "ERROR!", JOptionPane.WARNING_MESSAGE);
                e.printStackTrace();
            }
        }
        else if (evt.getActionCommand().contains("List"))
        {
            initScriptWindow();

            m_sfSetting.setDefaultList(m_jaScripts);
            m_sfSetting.setVisible(true);
        }
        else if (evt.getActionCommand().contains("About"))
        {
            JOptionPane.showMessageDialog(null,
                    "Build : " + APP_VERSION + "\n" +
                                "License : Apache License 2.0\n" +
                                "Creator : JOONOFAFA\n" +
                                "GitHub : https://github.com/joonofafa/", "About Text-Inverter ..", JOptionPane.INFORMATION_MESSAGE);
        }
        else if (evt.getActionCommand().contains("DO INVERT"))
        {
            String strValue = m_mfInstance.getText();
            if (strValue != null && strValue.length() > 0)
            {
                if (m_jaScripts == null)
                {
                    JOptionPane.showMessageDialog(m_mfInstance, "Nothing script!", "ERROR!", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (m_jaScripts.length() == 0)
                {
                    JOptionPane.showMessageDialog(m_mfInstance, "Nothing script!", "ERROR!", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int iFindCount = 0, iStartOffset = -1;
                String strFindWhat, strInvertWhat;

                try
                {
                    for (int i = 0; i < m_jaScripts.length(); i++)
                    {
                        JSONObject jsonTemp = m_jaScripts.getJSONObject(i);

                        strFindWhat = convertSpecialString(jsonTemp.getString("find"));
                        strInvertWhat = jsonTemp.getString("invert");

                        if (jsonTemp.getInt("ignore-space") == 1)
                        {
                            strFindWhat = strFindWhat.trim();
                        }

                        while ((iStartOffset = strValue.indexOf(strFindWhat, iStartOffset + 1)) >= 0)
                        {
                            iFindCount++;
                        }

                        if (jsonTemp.getInt("ignore-case") == 1)
                        {
                            strValue = strValue.replaceAll("(?i)" + strFindWhat.toLowerCase(), strInvertWhat);
                        } else
                        {
                            strValue = strValue.replaceAll(strFindWhat, strInvertWhat);
                        }
                    }
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(m_mfInstance, "An ERROR occurred in Inverting work. [" + e.getMessage() + "]", "ERROR!", JOptionPane.WARNING_MESSAGE);
                }

                JOptionPane.showMessageDialog(m_mfInstance,
                        "Script Loop : " + m_jaScripts.length() + " times\n" +
                                  "Invert : " + iFindCount + " times", "RESULT", JOptionPane.INFORMATION_MESSAGE);

                m_mfInstance.setText(strValue);
            }
            else
            {
                JOptionPane.showMessageDialog(m_mfInstance, "Nothing contents!", "ERROR!", JOptionPane.WARNING_MESSAGE);
            }
        }
    };

    public static void main(String saArgs[])
    {
        if (m_mfInstance == null)
        {
            m_mfInstance = new MainFrame();

            m_mfInstance.setTitle(APP_NAME + "[" + APP_NONAME_FILE + "]");
            m_mfInstance.setDefault();
            m_mfInstance.setSize(800, 600);
            m_mfInstance.setLocation(
                    (m_dScreenSize.width - m_mfInstance.getWidth()) / 2, (m_dScreenSize.height - m_mfInstance.getHeight()) / 2);

            MainMenuBar mmbTemp = new MainMenuBar(m_alEventHandler);
            m_mfInstance.setJMenuBar(mmbTemp.getMenuBar());
            m_mfInstance.setPopupMenu(mmbTemp.getPopupBar());
        }

        m_mfInstance.setVisible(true);
    }
}
