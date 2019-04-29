package com.xiaowei.mapper.dataobject;

import com.xiaowei.mapper.dataobject.TestDbDO;

/**
 * The table TEST_DB
 * 注意:此结构有系统生成,禁止手工修改,以免被覆盖,请通过dalgen生成
 */
public class TestDbDO{

    /**
     * id ID.
     */
    private Long id;
    /**
     * name NAME.
     */
    private String name;

    /**
     * Set id ID.
     */
    public void setId(Long id){
        this.id = id;
    }

    /**
     * Get id ID.
     *
     * @return the string
     */
    public Long getId(){
        return id;
    }

    /**
     * Set name NAME.
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * Get name NAME.
     *
     * @return the string
     */
    public String getName(){
        return name;
    }
}
