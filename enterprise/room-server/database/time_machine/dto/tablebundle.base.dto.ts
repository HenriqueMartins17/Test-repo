import {UserBaseInfoDto} from "../../../../user/dtos/user.dto";

export class TablebundleBaseDto {
    id!: string;
    isDeleted!: boolean;
    createdBy!: string;
    createdAt!: Date;
    spaceId!: string;
    dstId!: string;
    tbdId!: string;
    tablebundleUrl!: string;
    name!: string;
    type!: number;
    statusCode!: number;
    deletedBy!: string;
    deletedAt!: Date;
    creatorInfo!: UserBaseInfoDto | undefined;
    deleteInfo: any;
}

