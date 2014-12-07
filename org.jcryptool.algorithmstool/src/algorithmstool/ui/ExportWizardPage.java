package algorithmstool.ui;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ExportWizardPage extends WizardPage {
	private Text text_path;
	private Text text_col;
	private String colSep;
	private String pathSep;

	/**
	 * Create the wizard.
	 */
	public ExportWizardPage(String pathSep, String colSep) {
		super("Export Page"); //$NON-NLS-1$
		this.colSep = colSep;
		this.pathSep = pathSep;
		setTitle(Messages.ExportWizardPage_1);
		setDescription(Messages.ExportWizardPage_2);
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(2, false));
		
		Label lblPfadseparator = new Label(container, SWT.NONE);
		lblPfadseparator.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPfadseparator.setText(Messages.ExportWizardPage_3);
		
		text_path = new Text(container, SWT.BORDER);
		text_path.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text_path.setText(replaceTabIn(pathSep));
		
		Label lblSeparatorZwischenDen = new Label(container, SWT.NONE);
		lblSeparatorZwischenDen.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSeparatorZwischenDen.setText(Messages.ExportWizardPage_4);
		
		text_col = new Text(container, SWT.BORDER);
		text_col.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text_col.setText(replaceTabIn(colSep));
	}

	private static String replaceTabOut(String enteredText) {
		return enteredText.replace("\\t", "\t"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private static String replaceTabIn(String separatorToDisplay) {
		return separatorToDisplay.replace("\t", "\\t"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String getPathSep() {
		return replaceTabOut(text_path.getText());
	}
	
	public String getColSep() {
		return replaceTabOut(text_col.getText());
	}
	
}
