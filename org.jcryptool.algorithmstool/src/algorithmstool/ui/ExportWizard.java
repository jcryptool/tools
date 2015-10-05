package algorithmstool.ui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.jcryptool.core.logging.utils.LogUtil;
import org.jcryptool.core.operations.editors.AbstractEditorService;
import org.jcryptool.core.operations.editors.EditorsManager;

import algorithmstool.model.AlgorithmDescr;
import algorithmstool.model.IAlgorithmDescr;

public class ExportWizard extends Wizard {

	public static String lastPathSep;
	public static String lastColSep;
	
	private String initPathSep;
	private String initColSep;
	private List<? extends IAlgorithmDescr> algos;
	private ExportWizardPage page;

	public ExportWizard(String pathSep, String colSep, List<? extends IAlgorithmDescr> algos) {
		this.initPathSep = pathSep;
		this.initColSep = colSep;
		this.algos = algos;
		setWindowTitle(Messages.ExportWizard_0);
	}

	@Override
	public void addPages() {
		this.page = new ExportWizardPage(initPathSep, initColSep);
		this.addPage(this.page);
	}

	@Override
	public boolean performFinish() {
		ExportWizard.lastPathSep = this.page.getPathSep();
		ExportWizard.lastColSep = this.page.getColSep();
		writeToEditor(algos, this.page.getPathSep(), this.page.getColSep());
		MessageDialog.openInformation(getShell(), Messages.ExportWizard_1, Messages.ExportWizard_2);
		return true;
	}

	private static void writeToEditor(List<? extends IAlgorithmDescr> algos,
			String pathSep, String colSep) {
		String text = formatTable(algos, pathSep, colSep); 
		try {
            InputStream cis = new ByteArrayInputStream(text.getBytes("UTF-8")); //$NON-NLS-1$
            IEditorInput output = AbstractEditorService.createOutputFile(cis);
            EditorsManager.getInstance().openNewTextEditor(output);
        } catch (UnsupportedEncodingException usEx) {
            LogUtil.logError(usEx);
        } catch (PartInitException piEx) {
        	LogUtil.logError(piEx);
        }
	}

	private static String formatTable(List<? extends IAlgorithmDescr> algos,
			String pathSep, String colSep) {
		StringBuffer sb = new StringBuffer();
		for(IAlgorithmDescr a: algos) {
			sb.append(a.getName());
			sb.append(colSep);
			
			sb.append(a.getPerspective());
			sb.append(colSep);
			
			String path = AlgorithmDescr.makePathString(a.getPath(), pathSep);
			sb.append(path);
			
			sb.append("\n"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	public static String exportLastDefault(List<IAlgorithmDescr> selectedAlgos) {
		return formatTable(selectedAlgos, lastPathSep, lastColSep);
	}

}
