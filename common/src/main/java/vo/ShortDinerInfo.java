package vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Liu Ming on 2021/01/16.
 */
@Getter
@Setter
@ApiModel(description = "关注食客信息")
public class ShortDinerInfo implements Serializable {

    @ApiModelProperty("主键")
    public Integer id;
    @ApiModelProperty("昵称")
    private String nickname;
    @ApiModelProperty("头像")
    private String avatarUrl;

}
