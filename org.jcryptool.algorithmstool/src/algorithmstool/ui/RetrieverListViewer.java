package algorithmstool.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import algorithmstool.model.AlgorithmDescr;
import algorithmstool.model.IAlgorithmDescr;
import algorithmstool.model.retrievers.Retriever;

public class RetrieverListViewer extends Composite {


	public static final String SEP = " > "; //$NON-NLS-1$
	private List<Retriever> retriever;
	private Table table;
	private TableColumn tcName;
	private TableColumn tcPerspective;
	private TableColumn tcPath;
	
	private List<IAlgorithmDescr> retrievedAlgos;
	private List<IAlgorithmDescr> sortedAlgos;
	
	private Map<IAlgorithmDescr, Retriever> origins;
	
	private List<TableItem> tableItems;
	private Map<TableItem, IAlgorithmDescr> tiOrigins;
	private Observer o;
	
	Comparator<? super IAlgorithmDescr> sorting;
	int sortSign = 1;
	
	Comparator<? super IAlgorithmDescr> sName;
	Comparator<? super IAlgorithmDescr> sPerspective;
	Comparator<? super IAlgorithmDescr> sPath;
	private Label countLbl;
	
	public int getSortSign() {
		return sortSign;
	}
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public RetrieverListViewer(Composite parent, int style, List<Retriever> retriever, Observer o) {
		super(parent, style);
		this.retriever = retriever;
		this.o = o;
		
		this.makeSorters();
		
		this.makeUI();
		this.retrieveList();
		this.sortAlgosWithCurrentParadigma();
		this.renderList(this.getSortedAlgos());
	}
	
	private void makeSorters() {
		this.sName = new Comparator<IAlgorithmDescr>() {
			@Override
			public int compare(IAlgorithmDescr arg0, IAlgorithmDescr arg1) {
				return getSortSign() * ( arg0.getName().compareTo(arg1.getName()) );
			}
		};
		this.sPath = new Comparator<IAlgorithmDescr>() {
			@Override
			public int compare(IAlgorithmDescr arg0, IAlgorithmDescr arg1) {
				return getSortSign() * ( makePathString(arg0).compareTo(makePathString(arg1)) );
			}
		};
		this.sPerspective = new Comparator<IAlgorithmDescr>() {
			@Override
			public int compare(IAlgorithmDescr arg0, IAlgorithmDescr arg1) {
				return getSortSign() * ( arg0.getPerspective().compareTo(arg1.getPerspective()) );
			}
		};
	}

	public void setRetrievers(List<? extends Retriever> rs) {
		this.retriever = new LinkedList<Retriever>(rs);
		this.retrieveList();
		this.sortAlgosWithCurrentParadigma();
		this.renderList(getSortedAlgos());
	}
	
	private void sortAlgosWithCurrentParadigma() {
		this.sortedAlgos = new LinkedList<IAlgorithmDescr>(this.retrievedAlgos);
		if(sorting == null) return;

		Collections.sort(this.sortedAlgos, sorting);
	}

	private void notifyObserver() {
		if(o!=null) this.o.update(null, null);
	}

	private void renderList(List<IAlgorithmDescr> as) {
		if(this.tableItems != null) {
			for(TableItem ti: this.tableItems) {
				ti.dispose();
			}
		}
		this.tableItems = new LinkedList<TableItem>();
		this.tiOrigins = new HashMap<TableItem, IAlgorithmDescr>();
		
		for(IAlgorithmDescr a: as) {
			final TableItem ti = new TableItem(table, SWT.NONE);
			
			ti.addListener(SWT.MouseDoubleClick, new Listener() {
				@Override
				public void handleEvent(Event event) {
					
				}
			});
			ti.setText(new String[]{
					a.getName(),
					a.getPerspective(),
					makePathString(a)
			});
			
			this.tiOrigins.put(ti, a);
			this.tableItems.add(ti);
		}
		
		countLbl.setText(Messages.RetrieverListViewer_0 + as.size());
		tcPath.pack();
	}

	private String makePathString(IAlgorithmDescr a) {
		return AlgorithmDescr.makePathString(a.getPath(), SEP);
	}

	private void retrieveList() {
		this.retrievedAlgos = new LinkedList<IAlgorithmDescr>();
		this.origins = new HashMap<IAlgorithmDescr, Retriever>();
		
		for(Retriever r: this.retriever) {
			List<? extends IAlgorithmDescr> retrieve = r.retrieve();
			this.retrievedAlgos.addAll(retrieve);
			for(IAlgorithmDescr a: retrieve) {
				this.origins.put(a, r);
			}
		};
	}
	

	private void columnClicked(Comparator<? super IAlgorithmDescr> sorter) {
		Comparator<? super IAlgorithmDescr> currentSorter = RetrieverListViewer.this.sorting;
		
		if(currentSorter != sorter) {
			RetrieverListViewer.this.sorting = sorter;
			RetrieverListViewer.this.sortSign = 1;
		} else {
			RetrieverListViewer.this.sortSign = RetrieverListViewer.this.sortSign * (-1);
		}
		
		RetrieverListViewer.this.sortAlgosWithCurrentParadigma();
		RetrieverListViewer.this.renderList(RetrieverListViewer.this.getSortedAlgos());
	}
	
	protected void resetSorting() {
		RetrieverListViewer.this.sorting = null;
		RetrieverListViewer.this.sortSign = 1;
		
		RetrieverListViewer.this.sortAlgosWithCurrentParadigma();
		RetrieverListViewer.this.renderList(RetrieverListViewer.this.getSortedAlgos());
	}
	
	private void makeUI() {
		this.setLayout(new GridLayout(1, false));
		
		countLbl = new Label(this, SWT.NONE);
		countLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		table = new Table(this, SWT.MULTI | SWT.H_SCROLL
			      | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		
		tcName = new TableColumn(table, SWT.NONE);
		tcName.setText(Messages.RetrieverListViewer_1);
		tcName.setWidth(250);
		tcName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Comparator<? super IAlgorithmDescr> sorter = RetrieverListViewer.this.sName;
				if((e.stateMask & SWT.CTRL)==SWT.CTRL) {
					resetSorting();
					return;
				}
				columnClicked(sorter);
			}
		});
		
		tcPerspective = new TableColumn(table, SWT.NONE);
		tcPerspective.setText(Messages.RetrieverListViewer_2);
		tcPerspective.setWidth(80);
		tcPerspective.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Comparator<? super IAlgorithmDescr> sorter = RetrieverListViewer.this.sPerspective;
				if((e.stateMask & SWT.CTRL)==SWT.CTRL) {
					resetSorting();
					return;
				}
				columnClicked(sorter);
			}
		});
		
		tcPath = new TableColumn(table, SWT.NONE);
		tcPath.setText(Messages.RetrieverListViewer_3);
		tcPath.setWidth(600);
		tcPath.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Comparator<? super IAlgorithmDescr> sorter = RetrieverListViewer.this.sPath;
				if((e.stateMask & SWT.CTRL)==SWT.CTRL) {
					resetSorting();
					return;
				}
				columnClicked(sorter);
			}
		});
		
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				TableItem[] tis = table.getSelection();
				if(tis.length > 0) {
					TableItem item = tis[0];
					IAlgorithmDescr origAlgo = RetrieverListViewer.this.tiOrigins.get(item);
					Retriever origRetr = RetrieverListViewer.this.origins.get(origAlgo);
					boolean configured = origRetr.configure(getShell());
					if(configured) {
						RetrieverListViewer.this.notifyObserver();
					}
				}
			}
		});
		
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'c'))
                {
                    int[] idcs = table.getSelectionIndices();
					List<IAlgorithmDescr> selectedAlgos = new LinkedList<IAlgorithmDescr>();
					for(int idx: idcs) {
						selectedAlgos.add(sortedAlgos.get(idx));
					}
					String defaultToClipboard = ExportWizard.exportLastDefault(selectedAlgos);
					copyToClipboard(defaultToClipboard);
                }
			}
		});
		
	}

	protected void copyToClipboard(String defaultToClipboard) {
		final Clipboard cb = new Clipboard(getDisplay());
		TextTransfer textTransfer = TextTransfer.getInstance();
        cb.setContents(new Object[] { defaultToClipboard },
            new Transfer[] { textTransfer });
	}

	public List<IAlgorithmDescr> getSortedAlgos() {
		return sortedAlgos;
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
