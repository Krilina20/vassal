/*
 *
 * Copyright (c) 2008-2009 Brent Easton
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
/*
 * FormattedStringConfigurer.
 * Extended version of StringConfigure that provides a drop down list of options that can
 * be inserted into the string
 */
package VASSAL.script.expression;

import VASSAL.configure.Configurer;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.configure.BeanShellExpressionConfigurer;
import VASSAL.configure.StringConfigurer;
import VASSAL.counters.EditablePiece;
import VASSAL.tools.BrowserSupport;
import VASSAL.tools.ButtonFactory;

public class FunctionBuilder extends JDialog {

  private static final long serialVersionUID = 1L;
  protected String save;
  protected StringConfigurer target;
  protected String function;
  protected List<BeanShellExpressionConfigurer> configs = new ArrayList<>();
  protected EditablePiece targetPiece;

  public FunctionBuilder(StringConfigurer c, JDialog parent, String function, String desc, String[] parmDesc, EditablePiece piece, String[] hints, BeanShellExpressionConfigurer.Option[] options) {
    super(parent, "Function Builder - "+function, true);
    target = c;
    targetPiece = piece;
    save = target.getValueString();
    this.function = function;
    setLayout(new MigLayout("fillx,ins 0"));

    JPanel p = new JPanel(new MigLayout("wrap 1,fillx"));

    p.add(new JLabel(desc), "align center");
    for (int i=0; i < parmDesc.length; i++) {
      final BeanShellExpressionConfigurer config = new BeanShellExpressionConfigurer(null, parmDesc[i] + ":  ", "", targetPiece, options[i]);
      configs.add(config);
      p.add(config.getControls(), "align right,growx");
    }

    if (hints != null) {
      for (String hint : hints) {
        p.add(new JLabel(hint));
      }
    }

    JPanel buttonBox = new JPanel(new MigLayout("", "[]rel[]rel[]"));
    JButton okButton = ButtonFactory.getOkButton();
    okButton.addActionListener(e -> save());
    buttonBox.add(okButton);

    JButton cancelButton = ButtonFactory.getCancelButton();
    cancelButton.addActionListener(e -> cancel());
    buttonBox.add(cancelButton);

    JButton helpButton = ButtonFactory.getHelpButton();
    helpButton.addActionListener(e -> BrowserSupport.openURL(HelpFile.getReferenceManualPage("ExpressionBuilder.htm").getContents().toString()));
    buttonBox.add(helpButton);

    p.add(buttonBox, "align center");
    add(p, "growx");

    pack();
    setLocationRelativeTo(getParent());
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent we) {
        cancel();
      }
    });
  }

  /**
   * Ok button pressed. Set the expression back into the target configurer.
   * Note special handling for ternary "?" function. Only Ternary function, so no need to implement a general solution.
   */
  public void save() {
    StringBuilder result;

    if (function.equals("?")) {
      result = new StringBuilder("((" + configs.get(0).getValueString() + ") ? " + getExpr(configs.get(1)) + " : " + getExpr(configs.get(2)) + ")");
    }
    else {
      result = new StringBuilder(function + "(");
      boolean first = true;
      for (BeanShellExpressionConfigurer fec : configs) {
        if (!first) {
          result.append(",");
        }
        result.append(fec.getOption() == BeanShellExpressionConfigurer.Option.PME ? escape(fec.getValueString()): fec.getValueString());
        first = false;
      }
      result.append(")");
    }
    target.setValue(result.toString());
    dispose();
  }

  private String escape (String expr) {
    return "\"{" + expr.replace("\"", "\\\"") + "}\"";
  }

  private String getExpr (Configurer c) {
    final Expression e = Expression.createExpression("{"+c.getValueString()+"}");
    final boolean isAtomic = (e instanceof IntExpression) || (e instanceof StringExpression);
    return (isAtomic ? "" : "(") + c.getValueString() + (isAtomic ? "" : ")");
  }

  public void cancel() {
    dispose();
  }

}