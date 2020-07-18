import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;

public class ScriptFrame extends JDialog
{
    private JPanel pnScript;
    private JTextField txtInvertWhat;
    private JCheckBox chkIgnoreCase;
    private JCheckBox chkIgnoreSpace;
    private JButton btnAdd;
    private JButton btnRemove;
    private JButton btnClear;
    private JList lstScript;
    private JTextField txtFindWhat;

    private JSONArray m_jaScript = null;
    private int m_iCurrentSelected = -1;

    public void setDefault()
    {
        this.setTitle("Setting up script");
        this.setContentPane(pnScript);
        this.setModal(true);
        this.setResizable(false);

        btnAdd.addActionListener(m_alButtonMain);
        btnRemove.addActionListener(m_alButtonMain);
        btnClear.addActionListener(m_alButtonMain);

        lstScript.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void refreshList()
    {
        if (m_jaScript != null)
        {
            try
            {
                JSONObject jsonTemp;
                StringBuilder sbTemp;

                if (m_jaScript.length() > 0)
                {
                    Vector<String> vcValue = new Vector<>();

                    for (int i = 0; i < m_jaScript.length(); i++)
                    {
                        jsonTemp = m_jaScript.getJSONObject(i);
                        sbTemp = new StringBuilder();

                        sbTemp.append(String.format("Find [%s], ", jsonTemp.getString("find")));
                        sbTemp.append(String.format("Invert [%s], ", jsonTemp.getString("invert")));
                        sbTemp.append(String.format("Case [%s], ", (jsonTemp.getInt("ignore-case") == 1 ? "yes" : "no")));
                        sbTemp.append(String.format("Space [%s]", (jsonTemp.getInt("ignore-space") == 1 ? "yes" : "no")));

                        vcValue.addElement(sbTemp.toString());
                    }

                    lstScript.setListData(vcValue);
                    lstScript.addListSelectionListener(m_lsListenerMain);
                    lstScript.setSelectedIndex(0);
                }
                else
                {
                    lstScript.removeAll();
                    lstScript.setListData(new Vector<>());
                }
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, "Settings Data is wrong!", "ERROR!", JOptionPane.WARNING_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    public void setDefaultList(JSONArray jaParam)
    {
        m_jaScript = jaParam;

        if (m_jaScript != null)
        {
            refreshList();
        }
    }

    private boolean checkForm()
    {
        if (txtFindWhat.getText().length() <= 0)
        {
            JOptionPane.showMessageDialog(this, "[Find what] value is absent.", "ERROR!", JOptionPane.WARNING_MESSAGE);
            return true;
        }

        if (txtInvertWhat.getText().length() <= 0)
        {
            JOptionPane.showMessageDialog(this, "[Invert what] value is absent.", "ERROR!", JOptionPane.WARNING_MESSAGE);
            return true;
        }

        if (txtFindWhat.getText().equals(txtInvertWhat.getText()))
        {
            JOptionPane.showMessageDialog(this, "[Find what] text and [Invert what] text are same! (It should be different)", "ERROR!", JOptionPane.WARNING_MESSAGE);
            return true;
        }

        String strWord = txtFindWhat.getText();
        JSONObject jsonTemp;

        for (int i = 0; i < m_jaScript.length(); i++)
        {
            jsonTemp = m_jaScript.getJSONObject(i);

            if (jsonTemp.getString("find").equals(strWord))
            {
                JOptionPane.showMessageDialog(this, "[" + strWord + "] text already exist in Setting.", "ERROR!", JOptionPane.WARNING_MESSAGE);
                return true;
            }
        }

        return false;
    }

    private void clearForm()
    {
        txtFindWhat.setText("");
        txtInvertWhat.setText("");

        chkIgnoreCase.setSelected(false);
        chkIgnoreSpace.setSelected(false);

        txtFindWhat.requestFocus();
    }

    private final ActionListener m_alButtonMain = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getActionCommand().contains("Add"))
            {
                if (checkForm())
                {
                    return;
                }

                /**
                if (m_jaScript.length() > 0)
                {
                    JOptionPane.showMessageDialog(ScriptFrame.this, "Say 'Thank you' to LEE JINKYU. And then do receive a new version application HAHAHA!", "Fucking EGG!", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }**/

                JSONObject jsonTemp = new JSONObject();

                jsonTemp.put("find", txtFindWhat.getText());
                jsonTemp.put("invert", txtInvertWhat.getText());
                jsonTemp.put("ignore-case", chkIgnoreCase.isSelected() ? 1 : 0);
                jsonTemp.put("ignore-space", chkIgnoreSpace.isSelected() ? 1 : 0);

                m_jaScript.put(jsonTemp);
                clearForm();
                refreshList();
            }
            else if (e.getActionCommand().contains("Remove"))
            {
                int iResult = JOptionPane.showConfirmDialog(ScriptFrame.this,
                        "Would You Like to Remove this item?", "Caution! This Item Will Be Removed!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (iResult == JOptionPane.YES_OPTION)
                {
                    m_jaScript.remove(lstScript.getSelectedIndex());
                    refreshList();
                }
            }
            else if (e.getActionCommand().contains("Clear"))
            {
                int iResult = JOptionPane.showConfirmDialog(ScriptFrame.this,
                        "Would You Like to Clear all form?", "Warning! All Form Will Be Cleared!", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

                if (iResult == JOptionPane.YES_OPTION)
                {
                    clearForm();
                }
            }
        }
    };

    private final ListSelectionListener m_lsListenerMain = new ListSelectionListener()
    {
        @Override
        public void valueChanged(ListSelectionEvent evt)
        {
            if (m_iCurrentSelected != lstScript.getSelectedIndex())
            {
                if (lstScript.getModel().getSize() > 0)
                {
                    m_iCurrentSelected = lstScript.getSelectedIndex();
                    if (m_iCurrentSelected >= 0)
                    {
                        try
                        {
                            JSONObject jsonTemp = m_jaScript.getJSONObject(m_iCurrentSelected);

                            txtFindWhat.setText(jsonTemp.getString("find"));
                            txtInvertWhat.setText(jsonTemp.getString("invert"));
                            chkIgnoreCase.setSelected((jsonTemp.getInt("ignore-case") == 1));
                            chkIgnoreSpace.setSelected((jsonTemp.getInt("ignore-space") == 1));
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };
}
