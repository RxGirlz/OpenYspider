package com.devyy.openyspider.tujidao;

import javax.persistence.*;

/**
 * 图集岛-相册实体类
 *
 * @author zhangyiyang
 * @since 2019-09-12
 */
@Entity
@Table(name = "tbl_tujidao_album")
public class TujidaoDO {

    // <div class="c1">
    //   <ul>
    //     <li><span>2019-9-13</span><a href="http://www.tujidao.com/a/?id=29249">[57p] [Cosdoki] Yuzuna Aida 合田柚奈 aidayuzuna_pic_sailor2</a></li>
    //     <li><span>2019-9-13</span><a href="http://www.tujidao.com/a/?id=29248">[39p] [Cosdoki] Yuzuna Aida 合田柚奈 aidayuzuna_pic_sailor1</a></li>
    //   </ul>
    // </div>

    /**
     * ID 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 编号
     * eg.27691
     */
    @Column(name = "number")
    private Integer number;

    /**
     * 总数
     * eg.58（0~58）
     */
    @Column(name = "total")
    private Integer total;

    /**
     * 标题
     */
    @Column(name = "title")
    private String title;

    /**
     * 所属机构
     */
    @Column(name = "type")
    private String type;


    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
