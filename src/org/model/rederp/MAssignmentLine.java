package org.model.rederp;

import java.io.File;
import java.math.BigDecimal;
import java.util.Properties;

import org.compiere.process.DocAction;

public class MAssignmentLine extends X_RED_Assignment_Line implements DocAction {

	private static final long serialVersionUID = -7899079406308321250L;

	public MAssignmentLine(Properties ctx, int RED_Assignment_Line_ID, String trxName, String[] virtualColumns) {
		super(ctx, RED_Assignment_Line_ID, trxName, virtualColumns);
		// TODO Auto-generated constructor stub
	}

	public MAssignmentLine(Properties ctx, int RED_Assignment_Line_ID, String trxName) {
		super(ctx, RED_Assignment_Line_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setDocStatus(String newStatus) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDocStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean processIt(String action) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unlockIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean invalidateIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String prepareIt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean approveIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean rejectIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String completeIt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean voidIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean closeIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reverseCorrectIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reverseAccrualIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reActivateIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentNo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File createPDF() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProcessMsg() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDoc_User_ID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getC_Currency_ID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BigDecimal getApprovalAmt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocAction() {
		// TODO Auto-generated method stub
		return null;
	}


}
