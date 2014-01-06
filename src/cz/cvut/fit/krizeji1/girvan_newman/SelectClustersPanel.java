package cz.cvut.fit.krizeji1.girvan_newman;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

class SelectClustersPanel extends JPanel implements ActionListener {

  private final JComboBox combo;
  private int selected;

  public SelectClustersPanel(java.util.Vector items) {
    this.combo = new JComboBox(items);
    this.combo.addActionListener(this);

    this.add(new JLabel("Select number of clusters"));
    this.add(combo);
  }

  public int getSelectedClustersCount() {
    return this.selected;
  }

  @Override
  public void actionPerformed(ActionEvent ae) {
    selected = (Integer) combo.getSelectedItem();
  }
}
