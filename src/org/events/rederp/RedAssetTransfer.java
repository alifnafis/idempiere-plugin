package org.events.rederp;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;
import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MAsset;
import org.compiere.model.MAssetAddition;
import org.compiere.model.MAssetDisposed;
import org.compiere.model.MAssetGroup;
import org.compiere.model.MDepreciationWorkfile;
//import org.compiere.model.MDepreciationWorkfile;
import org.compiere.model.PO;
import org.compiere.model.PO_Record;
import org.compiere.model.X_A_Asset_Disposed;
import org.compiere.model.X_A_Depreciation_Workfile;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;
import org.model.rederp.X_RED_Asset_Transfer;
import org.osgi.service.event.Event;

public class RedAssetTransfer extends AbstractEventHandler {
  private Trx m_trx;

  @Override
  protected void doHandleEvent(Event event) {
    PO po = getPO(event);
    Integer depreWorkFileIDOne = 0;
    Integer aBalanceOrgIDOne = 0;
    m_trx = Trx.get(Trx.createTrxName("TRF"), true);
    if (event.getTopic().equals(IEventTopics.DOC_BEFORE_COMPLETE)) {
      if (po.get_TableName().equals(X_RED_Asset_Transfer.Table_Name)) {
        StringBuilder errorMessage = new StringBuilder(
          "Please input the data in Column: "
        );
        StringBuilder err = new StringBuilder();
        try {
          Timestamp now = new Timestamp(System.currentTimeMillis());
          Integer orgID = (Integer) po.get_Value("AD_Org_ID");
          Integer assetID = (Integer) po.get_Value("A_Asset_ID");
          Integer docTypeID = (Integer) po.get_Value("C_DocType_ID");
          int num = 0;
          if (orgID == null) {
            num++;
            err.append(num + ". Transfer to Organization  ");
          }

          if (assetID == null) {
            num++;
            err.append(num + ". Asset  ");
          }
          if (docTypeID == null) {
            num++;
            err.append(num + ". Document Type  ");
          }
          if (err.length() > 0) {
            throw new AdempiereException(
              errorMessage.toString() + err.toString()
            );
          }

          m_trx.start();

          MAsset originalAsset = new MAsset(
            Env.getCtx(),
            assetID,
            m_trx.getTrxName()
          );
          String originalValue = originalAsset.getValue();
          String originalName = originalAsset.getName();
          String originalInventoryNo = originalAsset.getInventoryNo();
          String originalDescription = originalAsset.getDescription();
          MAssetGroup originalAssetGroup = originalAsset.getAssetGroup();
          System.out.println(originalAssetGroup.get_ID());
          int originalM_Product_ID = originalAsset.getM_Product_ID();
          int originalManufacturedYear = originalAsset.getManufacturedYear();
          Timestamp originalCreated = originalAsset.getCreated();
          Timestamp originalAssetServiceDate = originalAsset.getAssetServiceDate();

          int assetTODisposed = originalAsset.get_ID();
          MAsset newAsset = new MAsset(Env.getCtx(), 0, m_trx.getTrxName());
          newAsset.setName("TRF - " + originalName);
          newAsset.setValue("TRF - " + originalName);
          newAsset.setAD_Org_ID(12);
          newAsset.setAssetActivationDate(now);
          newAsset.setIsActive(true);
          newAsset.setA_Asset_Status(MAsset.A_ASSET_STATUS_New);
          newAsset.setValue(originalValue);
          newAsset.setInventoryNo(originalInventoryNo);
          newAsset.setDescription(originalDescription);
          newAsset.setAssetGroup(originalAssetGroup);
          newAsset.setM_Product_ID(originalM_Product_ID);
          newAsset.setManufacturedYear(originalManufacturedYear);
          newAsset.setA_Asset_CreateDate(originalCreated);
          newAsset.setAssetServiceDate(originalAssetServiceDate);
          MAssetDisposed assetDisposed = new MAssetDisposed(
            Env.getCtx(),
            0,
            m_trx.getTrxName()
          );
          assetDisposed.setC_DocType_ID(200003);
          assetDisposed.setA_Asset_ID(assetTODisposed);
          assetDisposed.setA_Disposed_Method(
            X_A_Asset_Disposed.A_DISPOSED_METHOD_Trade
          );
          newAsset.saveEx(m_trx.getTrxName());
          int newAssetId = newAsset.get_ID();
          int originalAssetID = originalAsset.get_ID();
          String sql = "SELECT *,\n"
                  + "  (CASE\n"
                  + "    WHEN UseLifeMonths_F - (CASE WHEN A_Current_Period = 0 THEN 1 ELSE A_Current_Period END) + 1 > 0\n"
                  + "    THEN (A_Asset_Cost - A_Accumulated_Depr_F) / (UseLifeMonths_F - (CASE WHEN A_Current_Period = 0 THEN 1 ELSE A_Current_Period END) + 1)\n"
                  + "    ELSE 0\n"
                  + "  END) AS a_expense_sl_f\n"
                  + "FROM A_Depreciation_Workfile\n"
                  + "WHERE A_Asset_ID = ?";
          PreparedStatement pstmt = null;
          ResultSet rs = null;
          Object[] row = null;
          ResultSetMetaData metaData = null;
          try {
            pstmt = DB.prepareStatement(sql, null);
            pstmt.setInt(1, originalAssetID);
            rs = pstmt.executeQuery();

            if (rs.next()) {
            	 metaData = rs.getMetaData();
                 int columnCount = metaData.getColumnCount();
                 row = new Object[columnCount];
                 for (int i = 1; i <= columnCount; i++) {
                     row[i - 1] = rs.getObject(i);
                 }
            }
          } catch (SQLException e) {
            e.printStackTrace();
          } finally {
            DB.close(rs, pstmt);
          }
          BigDecimal assetCostRemain = new BigDecimal(row[37].toString());
          BigDecimal accumulatedDepr = new BigDecimal(row[29].toString());
          BigDecimal expenseSL = new BigDecimal(row[45].toString());
          BigDecimal currentPeriod = new BigDecimal(row[27].toString());
          BigDecimal expense = assetCostRemain.subtract(expenseSL);
          int currentPeriodInt = currentPeriod.intValue();
          currentPeriodInt = currentPeriodInt - 1;
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(now);
          calendar.add(Calendar.MONTH, currentPeriodInt);
          Timestamp newDate = new Timestamp(calendar.getTimeInMillis());
          assetDisposed.setDateAcct(newDate);
          assetDisposed.setDateDoc(newDate);
          assetDisposed.setA_Disposed_Date(newDate);
          assetDisposed.setA_Asset_Status("AC");
          assetDisposed.setIsDisposed(false);
          assetDisposed.setA_Accumulated_Depr(accumulatedDepr);
          assetDisposed.setA_Accumulated_Depr_Delta(expenseSL);
          assetDisposed.setA_Disposal_Amt(assetCostRemain);
          assetDisposed.setExpense(expense);
          assetDisposed.setPostingType("A");
          assetDisposed.saveEx(m_trx.getTrxName());
          String statusAssetDispo = assetDisposed.completeIt();
          assetDisposed.setDocStatus(statusAssetDispo);
          assetDisposed.saveEx(m_trx.getTrxName());

       BigDecimal assetCost = new BigDecimal(row[4].toString());
       BigDecimal currentQtyDecimal = new BigDecimal(row[17].toString());
       System.out.println(row[16]);
       BigDecimal salvageValueDecimal;
       if (row[16].toString().equals("0")) {
           salvageValueDecimal = BigDecimal.ZERO;
           System.out.println(salvageValueDecimal);
           System.out.println(salvageValueDecimal);
       } else {
           salvageValueDecimal = new BigDecimal(row[16].toString());
       }

          MAssetAddition assetAddition = new MAssetAddition(
            Env.getCtx(),
            0,
            m_trx.getTrxName()
          );
          assetAddition.setA_Asset_ID(newAssetId);
          assetAddition.setAD_Org_ID(orgID);
          assetAddition.setA_CreateAsset(true);
          assetAddition.setC_DocType_ID(1000001);
          assetAddition.setC_Currency_ID(100);
          assetAddition.setA_CapvsExp("Cap");
          assetAddition.setM_Product_ID(originalM_Product_ID);
          assetAddition.setA_SourceType(MAssetAddition.A_SOURCETYPE_Manual);
          assetAddition.setAssetAmtEntered(assetCost);
          assetAddition.setA_QTY_Current(currentQtyDecimal);
          assetAddition.setA_Salvage_Value(salvageValueDecimal);
          assetAddition.setAssetValueAmt(assetCost);
          assetAddition.setSourceAmt(assetCost);
          assetAddition.setDateAcct(now);
          assetAddition.setDateDoc(now);
          assetAddition.saveEx(m_trx.getTrxName());
          String completeAdd = assetAddition.completeIt();
          assetAddition.setDocStatus(completeAdd);
          assetAddition.saveEx(m_trx.getTrxName());
//		PO_Record test = new PO_Record();
//		System.out.println(test);
//          String sqlUpdate =
//                  "SELECT a_depreciation_workfile_id, ad_org_id FROM A_Depreciation_Workfile WHERE A_Asset_ID = ?";
//          PreparedStatement pstmtUpdate = null;
//          ResultSet rsUpdate = null;
//          Object[] rowUpdate = null;
//                try {
//                  pstmtUpdate = DB.prepareStatement(sqlUpdate, null);
//                  pstmtUpdate.setInt(1, newAssetId);
//                  rsUpdate = pstmtUpdate.executeQuery();
//
//                  if (rsUpdate.next()) {
//                	  rowUpdate = new Object[3]; // Replace '3' with the number of columns you expect to retrieve
//                      rowUpdate[0] = rsUpdate.getObject("a_depreciation_workfile_id");
//                      rowUpdate[1] = rsUpdate.getObject("ad_org_id");
//                  }
//                } catch (SQLException e) {
//                  e.printStackTrace();
//                } finally {
//                  DB.close(rsUpdate, pstmtUpdate);
//                }
//                int depreWorkFileID = (int) rowUpdate[0];
//                int aBalanceOrgID = (int) rowUpdate[2];
                
          MDepreciationWorkfile assetBalanceUpdate = new MDepreciationWorkfile(Env.getCtx(), depreWorkFileIDOne, null);
//        System.out.println(assetBalance.isDepreciated(getDateAcct()));
          Integer testing = assetBalanceUpdate.get_ID();
//          System.out.println(depreWorkFileID);
//          System.out.println(aBalanceOrgID);
          System.out.println(testing + "testing");
     if (row != null && metaData != null) {
         int columnCount = row.length;
         for (int i = 1; i <= columnCount; i++) {
             String columnName = metaData.getColumnName(i);
             Object columnValue = row[i - 1];
             assetBalanceUpdate.set_ValueOfColumn(columnName, columnValue);
         }
     }
     assetBalanceUpdate.setA_Depreciation_Workfile_ID(depreWorkFileIDOne);
     assetBalanceUpdate.setAD_Org_ID(aBalanceOrgIDOne);
     assetBalanceUpdate.saveEx(m_trx.getTrxName());
     m_trx.commit();
        } catch (Exception e) {
          m_trx.rollback();
          e.printStackTrace();
          throw new AdempiereException(e);
        } finally {
          m_trx.close();
        }
      }
    } else if (event.getTopic().equals(IEventTopics.PO_AFTER_NEW)) {
    	if (po.get_TableName().equals(X_A_Depreciation_Workfile.Table_Name)) {
    	depreWorkFileIDOne = (Integer) po.get_Value("a_depreciation_workfile_id");
    	aBalanceOrgIDOne = (Integer) po.get_Value("ad_org_id");
    	}
    }
  }

  @Override
  protected void initialize() {
    registerTableEvent(
      IEventTopics.DOC_BEFORE_COMPLETE,
      X_RED_Asset_Transfer.Table_Name
    );
    registerTableEvent(
    	      IEventTopics.PO_AFTER_NEW,
    	      X_A_Depreciation_Workfile.Table_Name
    	    );
  }
}
