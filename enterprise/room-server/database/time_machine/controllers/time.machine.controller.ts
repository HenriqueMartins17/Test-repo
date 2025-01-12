import {
  Body,
  Controller,
  Delete,
  Get,
  Headers,
  Param,
  Post,
  Res,
  Put,
  Query,
  UseInterceptors,
  Header
} from '@nestjs/common';
import fs, {readFileSync} from "fs";
import { Response } from 'express';
import {TablebundleBaseDto} from '../dto/tablebundle.base.dto';
import { UserService } from 'user/services/user.service';
import { NodeService } from 'node/services/node.service';
import {ResourceDataInterceptor} from '../../../../database/resource/middleware/resource.data.interceptor';
import {TimeMachineBaseService} from "../../../../database/time_machine/time.machine.service.base";
@Controller('nest/v1')
export class TimeMachineController {
  constructor(
      private readonly userService: UserService,
      private readonly nodeService: NodeService,
      private readonly timeMachineBaseService: TimeMachineBaseService,
  ) {
  }

  /**
   * create table bundle
   * */
  @Post('nodes/:nodeId/tablebundles')
  @UseInterceptors(ResourceDataInterceptor)
  async createSnapshot(
      @Headers('cookie') cookie: string,
      @Param('nodeId') nodeId: string
  ){
    const { userId } = await this.userService.getMe({ cookie });
    const spaceId = await this.nodeService.checkUserForNode(userId, nodeId);
    await this.nodeService.checkNodePermission(nodeId, { cookie });
    return this.timeMachineBaseService.generateTableBundle(cookie, nodeId, spaceId, userId);
  }

  @Post('nodes/:nodeId/tablebundles/get')
  @UseInterceptors(ResourceDataInterceptor)
  async creategSnapshot(
      @Headers('cookie') cookie: string,
      @Param('nodeId') nodeId: string
  ){
    return await this.timeMachineBaseService.getDataPack(cookie, nodeId, 'spaceId', 'userId');
  }

  /**
   * update table bundle info
   * */
  @Put('nodes/:nodeId/tablebundles/:tablebundleId')
  @UseInterceptors(ResourceDataInterceptor)
  async updateSnapshot(
      @Headers('cookie') cookie: string,
      @Param('tablebundleId') tablebundleId: string,
      @Param('nodeId') nodeId: string,
      @Body() body: { name: string }
  ){
    await this.nodeService.checkNodePermission(nodeId, { cookie });
    if (body.name) {
      await this.timeMachineBaseService.renameTableBundle(tablebundleId, body.name);
    }
    return null;
  }

  /**
   * delete table bundle info
   * */
  @Delete('nodes/:nodeId/tablebundles/:tablebundleId')
  @UseInterceptors(ResourceDataInterceptor)
  async deleteSnapshot(
      @Headers('cookie') cookie: string,
      @Param('nodeId') nodeId: string,
      @Param('tablebundleId') tablebundleId: string
  ){
    await this.nodeService.checkNodePermission(nodeId, { cookie });
    const { userId } = await this.userService.getMe({ cookie });
    return await this.timeMachineBaseService.deleteTableBundle(nodeId, tablebundleId, userId);
  }

  /**
   * preview table bundle info
   * */
  @Get('nodes/:nodeId/tablebundles/:tablebundleId/preview')
  @UseInterceptors(ResourceDataInterceptor)
  async previewSnapshot(
      @Headers('cookie') cookie: string,
      @Param('nodeId') nodeId: string,
      @Param('tablebundleId') tablebundleId: string
  ){
    await this.nodeService.checkNodePermission(nodeId, { cookie });
    return await this.timeMachineBaseService.previewTableBundle(tablebundleId, nodeId);
  }

  /**
   * recover table bundle info
   * */
  @Post('nodes/:dstId/tablebundles/:tablebundleId/recover')
  @UseInterceptors(ResourceDataInterceptor)
  async recoverSnapshot(
      @Headers('cookie') cookie: string,
      @Param('dstId') dstId: string,
      @Param('tablebundleId') tablebundleId: string,
      @Query('folderId') fldId: string,
      @Query('name') name: string,
  ){
    const { userId } = await this.userService.getMe({ cookie });
    const spaceId = await this.nodeService.checkUserForNode(userId, dstId);
    await this.nodeService.checkNodePermission(dstId, { cookie });
    return await this.timeMachineBaseService.recoverTableBundle(userId, tablebundleId, spaceId, dstId, fldId, name);
  }

  /**
   * query table bundle list
   * */
  @Get('nodes/:nodeId/tablebundles')
  @UseInterceptors(ResourceDataInterceptor)
  async listSnapshot(
      @Headers('cookie') cookie: string,
      @Param('nodeId') nodeId: string,
      @Query('tablebundleId') tablebundleId?: string,
  ): Promise<TablebundleBaseDto[]>{
    await this.nodeService.checkNodePermission(nodeId, { cookie });
    return await this.timeMachineBaseService.getTableBundleById(nodeId, tablebundleId);
  }

  /**
   * download table bundle file
   * */
  @Get('nodes/:nodeId/tablebundles/:tablebundleId/download')
  @UseInterceptors(ResourceDataInterceptor)
  @Header('Content-Type', 'application/zip')
  @Header('Content-Disposition', 'attachment; filename="package.tablebundle"')
  async downloadSnapshot(
      @Headers('cookie') cookie: string,
      @Param('tablebundleId') tablebundleId: string,
      @Param('nodeId') nodeId: string,
      @Res() response: Response
  )
  {
    await this.nodeService.checkNodePermission(nodeId, {cookie});
    const fileName = `${nodeId}.tablebundle`;
    const filePath = await this.timeMachineBaseService.downloadTableBundle(tablebundleId, nodeId, fileName);
    console.log('file', fileName, tablebundleId, cookie);
    response.send(readFileSync(filePath));
    fs.unlinkSync(filePath);
  }
}