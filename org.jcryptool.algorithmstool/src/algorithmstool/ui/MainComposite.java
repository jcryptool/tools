package algorithmstool.ui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.jcryptool.core.logging.utils.LogUtil;
import org.jcryptool.crypto.flexiprovider.algorithms.ui.views.providers.FlexiProviderAlgorithmsViewContentProvider;
import org.jcryptool.crypto.flexiprovider.ui.nodes.ITreeNode;

import algorithmstool.model.IAlgorithmDescr;
import algorithmstool.model.retrievers.Retriever;
import algorithmstool.model.retrievers.impl.DocViewCryptoRetriever;
import algorithmstool.model.retrievers.impl.DocViewRestRetriever;
import algorithmstool.model.retrievers.impl.FlexiProviderRetriever;

@SuppressWarnings("serial")
public class MainComposite extends Composite {

    private List<Retriever> retrievers;
    private DocViewCryptoRetriever retrDocViewAlgos;

    private DocViewRestRetriever retrAnalysis;

    private DocViewRestRetriever retrVis;

    private DocViewRestRetriever retrGames;

    private RetrieversViewer rV;

    private Observer retrieverCfgObserver;

    private RetrieverListViewer table;

    private Composite fpComp;
    private Group rVGroup;
    private List<FlexiProviderRetriever> fpRetrievers;
    private String defaultColSep = "\t"; //$NON-NLS-1$
    private String defaultPathSep = " > "; //$NON-NLS-1$

    static {
        ExportWizard.lastColSep = "\t"; //$NON-NLS-1$
        ExportWizard.lastPathSep = " > "; //$NON-NLS-1$
    }

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public MainComposite(Composite parent, int style) {
        super(parent, style);
        makeDefaultRetrievers();

        generateUI();
        renderDisplay();
    }

    private List<Retriever> makeDefaultRetrievers() {
        this.retrievers = new LinkedList<Retriever>();

        String stubDescrDoc = Messages.MainComposite_0;
        final List<String> stubPathDoc = new LinkedList<String>() {
            {
                add(Messages.MainComposite_1);
            }
        };

        String name;
        LinkedList<String> basePath;
        String descr;
        String extPt;

        name = Messages.MainComposite_2;
        basePath = appendList(stubPathDoc, name);
        descr = stubDescrDoc + Messages.MainComposite_3;
        retrDocViewAlgos = new DocViewCryptoRetriever(basePath, descr);
        retrievers.add(retrDocViewAlgos);

        extPt = "org.jcryptool.core.operations.analysis"; //$NON-NLS-1$
        name = Messages.MainComposite_5;
        basePath = appendList(stubPathDoc, name);
        descr = stubDescrDoc + name;
        retrAnalysis = new DocViewRestRetriever(basePath, extPt, descr);
        retrievers.add(retrAnalysis);

        extPt = "org.jcryptool.core.operations.visuals"; //$NON-NLS-1$
        name = Messages.MainComposite_7;
        basePath = appendList(stubPathDoc, name);
        descr = stubDescrDoc + name;
        retrVis = new DocViewRestRetriever(basePath, extPt, descr);
        retrievers.add(retrVis);

        extPt = "org.jcryptool.core.operations.games"; //$NON-NLS-1$
        name = Messages.MainComposite_9;
        basePath = appendList(stubPathDoc, name);
        descr = stubDescrDoc + name;
        retrGames = new DocViewRestRetriever(basePath, extPt, descr);
        retrievers.add(retrGames);

        loadFPRetrievers();

        return retrievers;
    }

    private void loadFPRetrievers() {
        tryAutomaticFPLoad();

        this.fpRetrievers = new LinkedList<FlexiProviderRetriever>();

        String name;
        List<String> basePath;
        String descr;

        String stubDescrDoc = "FP > "; //$NON-NLS-1$
        final List<String> stubPathDoc = new LinkedList<String>() {
            {
                add("[A]"); //$NON-NLS-1$
            }
        };

        ITreeNode root = FlexiProviderAlgorithmsViewContentProvider._invisibleRoot;
        for (Object n : root.getChildrenArray()) {
            ITreeNode node = (ITreeNode) n;

            name = node.getName();
            basePath = appendList(stubPathDoc, name);
            descr = stubDescrDoc + name;
            FlexiProviderRetriever retr = new FlexiProviderRetriever(basePath, node, descr);
            retrievers.add(retr);
            fpRetrievers.add(retr);
        }
    }

    private void tryAutomaticFPLoad() {
        try {
            IWorkbench wb = PlatformUI.getWorkbench();
            IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
            IWorkbenchPage page = win.getActivePage();
            IPerspectiveDescriptor perspective = page.getPerspective();

            String currentId = perspective.getId();
            String fpId = "org.jcryptool.crypto.flexiprovider.ui.perspective.FlexiProviderPerspective"; //$NON-NLS-1$

            PlatformUI.getWorkbench().showPerspective(fpId, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
            PlatformUI.getWorkbench().showPerspective(currentId, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
        } catch (WorkbenchException e) {
            LogUtil.logError(e);
        }
    }

    private LinkedList<String> appendList(final List<String> stubPathDoc, final String name) {
        return new LinkedList<String>() {
            {
                addAll(stubPathDoc);
                add(name);
            }
        };
    }

    private void generateUI() {

        this.retrieverCfgObserver = new Observer() {
            @Override
            public void update(Observable arg0, Object arg1) {
                renderDisplay();
            }
        };

        // -----------

        setLayout(new GridLayout(2, false));

        GridData rVGroupLayoutData = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
        createRetrieversViewerGroup(this, rVGroupLayoutData);

        GridData dispGroupLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        createDisplayGroup(this, dispGroupLayoutData);
    }

    private void createDisplayGroup(Composite parent, GridData dispGroupLayoutData) {
        Group dispGroup = new Group(parent, SWT.NONE);
        dispGroup.setLayout(new GridLayout());
        dispGroup.setLayoutData(dispGroupLayoutData);
        dispGroup.setText(Messages.MainComposite_12);

        this.table = new RetrieverListViewer(dispGroup, SWT.NONE, Collections.EMPTY_LIST, retrieverCfgObserver);
        this.table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label lblClickInfo = new Label(dispGroup, SWT.WRAP);
        GridData lblCILLD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        lblCILLD.widthHint = 600;
        lblClickInfo.setLayoutData(lblCILLD);
        lblClickInfo.setText(Messages.MainComposite_13);

        Label lblSortInfo = new Label(dispGroup, SWT.WRAP);
        GridData lblSI = new GridData(SWT.FILL, SWT.CENTER, true, false);
        lblSI.widthHint = 600;
        lblSortInfo.setLayoutData(lblSI);
        lblSortInfo.setText(Messages.MainComposite_14);
    }

    private void createRetrieversViewerGroup(Composite parent, GridData layoutData) {
        rVGroup = new Group(parent, SWT.NONE);
        rVGroup.setLayout(new GridLayout());
        rVGroup.setLayoutData(layoutData);
        rVGroup.setText(Messages.MainComposite_15);

        this.rV = new RetrieversViewer(rVGroup, SWT.None, this.retrievers, retrieverCfgObserver);
        this.rV.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button exportBtn = new Button(rVGroup, SWT.PUSH);
        exportBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        exportBtn.setText(Messages.MainComposite_16);
        exportBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MainComposite.this.exportClicked();
            }
        });

        if (FlexiProviderRetriever.canInitialize()) {

        } else {
            Composite borderC = new Composite(rVGroup, SWT.BORDER);
            borderC.setLayout(new GridLayout());
            borderC.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            makeFPLoadArea(borderC);
        }
    }

    private void makeFPLoadArea(Composite borderC) {
        fpComp = new Composite(borderC, SWT.NONE);
        fpComp.setLayout(new GridLayout(2, false));

        Label l = new Label(fpComp, SWT.WRAP);
        l.setText(Messages.MainComposite_17);
        GridData lData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        lData.widthHint = 250;
        l.setLayoutData(lData);

        Button b = new Button(fpComp, SWT.PUSH);
        b.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
        b.setText(Messages.MainComposite_18);
        b.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fpRefresh();
            }
        });

    }

    protected void fpRefresh() {
        loadFPRetrievers();
        for (Control c : this.getChildren()) {
            c.dispose();
        }
        this.generateUI();
        layout(true);
        this.renderDisplay();
    }

    protected void exportClicked() {
        List<IAlgorithmDescr> algos = new LinkedList<IAlgorithmDescr>(this.table.getSortedAlgos());
        ExportWizard wizard = new ExportWizard(defaultPathSep, defaultColSep, algos); // $NON-NLS-1$
                                                                                      // //$NON-NLS-2$
        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        int result = dialog.open();
        if (result == WizardDialog.OK) {
            this.defaultPathSep = ExportWizard.lastPathSep;
            this.defaultColSep = ExportWizard.lastColSep;
        }
    }

    protected void renderDisplay() {
        List<? extends Retriever> rs = getEnabledRetrievers();

        table.setRetrievers(rs);
    }

    private List<? extends Retriever> getEnabledRetrievers() {
        List<Retriever> result = new LinkedList<Retriever>();
        for (Retriever r : this.retrievers) {
            if (this.rV.isRetrieverSelected(r)) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

}
