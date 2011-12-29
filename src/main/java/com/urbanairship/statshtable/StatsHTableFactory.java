package com.urbanairship.statshtable;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableFactory;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTableInterfaceFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class StatsHTableFactory implements HTableInterfaceFactory {
    public enum OpType {
        GET, MULTIGET, GET_ROW_OR_BEFORE, PUT, BATCH, GET_SCANNER, NEXT, EXISTS, CHECK_AND_PUT, DELETE, 
        INCREMENT, CHECK_AND_DELETE
    }
    
    private final static Logger log = LogManager.getLogger(StatsHTableFactory.class);
    
    private final boolean useStats;
    private final String metricsScope;
    private final HTableInterfaceFactory normalHTableFactory = new HTableFactory();
    
    /**
     * @param useStats if true, will produce StatsHTable instances. If false, will produce normal HTable instances.
     * @param metricsScope the scope argument passed to all yammer metrics functions
     */
    public StatsHTableFactory(boolean useStats, String metricsScope) {
        this.useStats = useStats;
        this.metricsScope = metricsScope;
    }
    
    @Override
    public HTableInterface createHTableInterface(Configuration config, byte[] tableName) {
        HTable hTable = (HTable)normalHTableFactory.createHTableInterface(config, tableName);
        if(useStats) {
            return new StatsHTable(metricsScope, hTable);
        } else {
            return hTable;
        }
    }

    @Override
    public void releaseHTableInterface(HTableInterface table) {
        // Wraps the underlying normal htable factory. If the incoming table is a StatsHTable, unwrap it.
        HTableInterface hTableToRelease;
        if(table instanceof StatsHTable) {
            hTableToRelease = ((StatsHTable)table).unwrap();
        } else {
            hTableToRelease = table;
        }
        normalHTableFactory.releaseHTableInterface(hTableToRelease);
    }
}