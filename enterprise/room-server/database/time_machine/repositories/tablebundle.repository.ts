import { EntityRepository, Repository } from 'typeorm';
import {TableBundleEntity} from 'database/time_machine/entities/tablebundle.entity';

@EntityRepository(TableBundleEntity)
export class TableBundleRepository extends Repository<TableBundleEntity>{

}
