import {colors} from "@apitable/components";
import {rainbowRed5} from "@apitable/components/dist/colors/light";

export enum DataSourceStatus {
  Training,
  Success,
  Fail
}


export const COLOR_CONFIG ={
  [DataSourceStatus.Training]:{
    color:colors.textSuccessDefault,
    bgColor:colors.bgSuccessLightDefault,
    borderColor:colors.borderOnsuccessDefault,
    content:'Training'
  },
  [DataSourceStatus.Success]:{
    color:colors.textBrandDefault,
    bgColor:colors.bgBrandLightDefault,
    borderColor:colors.borderBrandDefault,
    content:'Success'
  },
  [DataSourceStatus.Fail]:{
    color:colors.rainbowRed5,
    bgColor:colors.bgDangerLightDefault,
    borderColor:colors.borderOndangerDefault,
    content:'Fail'
  }
}

