import SelectAirtable from './airtable';
import SelectUpload from './upload';
import SelectAitable from './aitable';

export enum TabKey {
  Airtable = 'Airtable',
  Local = 'Local',
  AITable = 'AITable',
}

export const DEFAULT_TAB_CONFIG = {
  [TabKey.Airtable]: SelectAirtable,
  [TabKey.Local]: SelectUpload,
  [TabKey.AITable]: SelectAitable,
};
