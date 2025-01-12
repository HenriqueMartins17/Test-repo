/**
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

const {
  Vika,
} = require('@vikadata/vika');
const https = require('https');
const fs = require('fs');
const path = require('path');
const envfile = require('envfile');
const TEMPLATE = require('./template');

const SPACE_ID = 'spczdmQDfBAn5';
const CONFIG_PACKAGE_ID = 'fodt3Nv5QPPNX';
const BUILD_PATH = './';

const sleep = (delay = 1000) => new Promise(res => setTimeout(res, delay));

const vika = new Vika({
  token: 'uskM7PcEPftF4wh0Ni1', // It is recommended to pass in the environment variable
  host: 'https://integration.vika.ltd/fusion/v1',
});

// Get the name of the privatized environment in the cmd parameter
const getEditionName = () => {
  const argv = process.argv?.slice(2);
  if (!Array.isArray(argv) || argv.length < 1) {
    throw new Error('Expected 1 arguments, but got 0');
  }

  if (argv.length > 1) {
    throw new Error(`Expected 1 arguments, but got ${argv.length}`);
  }

  return argv[0];
};

// Get all configuration tables for the corresponding deployment environment
const getSettingsDatasheets = async (envName) => {
  // Get details of a specified folder on a specified space station
  const privateEnvFoldersRes = await vika.nodes.get({spaceId: SPACE_ID, nodeId: CONFIG_PACKAGE_ID});
  if (privateEnvFoldersRes.success) {
    // Find all configuration tables for the corresponding deployment environment
    const folder = privateEnvFoldersRes.data.children?.find(fol => fol.name === envName);

    if (!folder) throw new Error(`Cannot find the folder named ${envName}`);

    console.log(`Found private env folder ${folder.id}`);

    await sleep(); // No sleep will report 429
    const privateEnvDstRes = await vika.nodes.get({spaceId: SPACE_ID, nodeId: folder.id});
    if (privateEnvDstRes.success) {
      if (privateEnvDstRes.data.children) return privateEnvDstRes.data.children;
      throw new Error('No configuration datasheets');
    }
    throw new Error(privateEnvDstRes.message);
  }
  console.log("err => ", privateEnvFoldersRes);
  throw new Error(privateEnvFoldersRes.message);
};

const getDatasheetData = async (datasheetId, setting) => {
  let records = [];

  try {
    for await (const eachPageRecords of vika.datasheet(datasheetId).records.queryAll({...setting})) {
      records = [...records, ...eachPageRecords];
    }
    return records;
  } catch (err) {
    console.error('Fail to fetch records');
    return [];
  }
};

// Download attachment build
const download = (url, fileName) => {
  const _path = path.resolve(`${BUILD_PATH}/public`);


  https.get(url, (res) => {
    const writeStream = fs.createWriteStream(path.resolve(`${_path}/${fileName}`));

    res.pipe(writeStream);

    writeStream.on('finish', () => {
      writeStream.close();
      console.log(`Successfully download ${fileName}`);
    });
  });
};

// Replace icons in the build directory, etc.
const fetchPublic = async (datasheetOfPublic) => {
  if (!datasheetOfPublic) {
    console.log('Public datasheet not found, skip');
    return;
  }

  const _path = path.resolve(`${BUILD_PATH}/public`);
  if (!fs.existsSync(_path)) {
    fs.mkdirSync(_path);
  }

  console.log(`Execute fetching ${datasheetOfPublic.name}`);

  const recordsOfPublic = await getDatasheetData(datasheetOfPublic.id);

  if (!recordsOfPublic?.length) {
    console.error(`No data under ${datasheetOfPublic.name}`);
    return;
  }

  recordsOfPublic.forEach((record) => {
    const {name, file} = record.fields;

    if (!name || !file) {
      console.error(`${record.recordId} has no name or file`);
      return;
    }

    const attachmentFile = file?.[0];

    if (!attachmentFile) {
      console.error(`${record.recordId} file has wrong data`);
      return;
    }

    download(attachmentFile.url, name);
  });
};

// Add private i18n content for files in lang directory
const fetchStrings = async (datasheetOfStrings, template) => {
  let _template = template;

  if (!datasheetOfStrings) {
    console.log('Strings datasheet not found, skip');

    return _template.replace('{{zh_CN}}', '{}').replace('{{en_US}}', '{}');
  }

  console.log(`Execute ${datasheetOfStrings.name} datasheet to generate custom language pack`);

  const recordsOfStrings = await getDatasheetData(datasheetOfStrings.id);

  if (!recordsOfStrings.length) {
    console.log('Strings datasheet has no data')

    return _template.replace('{{zh_CN}}', '{}').replace('{{en_US}}', '{}');
  }

  const langPacks = {};

  recordsOfStrings.forEach((record) => {
    const translation = record.fields;
    const {id, ...langs} = translation;

    Object.keys(langs).forEach(lang => {
      langPacks[lang] = {
        ...langPacks[lang],
        [id]: langs[lang],
      };
    });
  }, {});

  Object.keys(langPacks).forEach(lang => {
    const dict = langPacks[lang];
    const jsonDict = JSON.stringify(dict, null, 2);

    _template = _template.replace(`{{${lang}}}`, jsonDict);
  });

  console.log('Successfully generated language pack');

  return _template;
};

const fetchEnv = async (datasheetOfStrings) => {
  if (!datasheetOfStrings) {
    console.log('env datasheet not found, skip');

    return {};
  }

  console.log(`Execute ${datasheetOfStrings.name} datasheet to generate custom env`);

  const recordsOfEnvs = await getDatasheetData(datasheetOfStrings.id);

  if (!recordsOfEnvs.length) {
    console.log('env datasheet has no data')

    return {}
  }

  const settingsObject = recordsOfEnvs.reduce((acc, cur) => {
    if (cur?.fields) {
      return {
        ...acc,
        [cur.fields.id]: cur.fields.value,
      };
    }
  }, {});

  return settingsObject;
};

const generateEditionSettings = async (configDatasheets, _path) => {
  const baseEnvs = await fetchEnv({name: 'env_basic', id: 'dst2UMJr3wDdvScHmn'});
  const envs = await fetchEnv(configDatasheets.find(configDatasheet => configDatasheet.name === 'env'));
  const _envs = {...baseEnvs, ...envs}
  let parsedFile = {};

  for (const k in _envs) {
    parsedFile[`${k.toUpperCase()}`] = _envs[k] ?? '';
  }
  fs.writeFileSync(path.resolve(`${_path}/.env`), envfile.stringify(parsedFile));
};

const main = async () => {
  try {
    const editionName = getEditionName();

    const settingsDatasheets = await getSettingsDatasheets(editionName);

    // public files
    await fetchPublic(settingsDatasheets.find(configDatasheet => configDatasheet.name === 'public'));

    let template = TEMPLATE;
    const _path = path.resolve(`${BUILD_PATH}/custom`);
    template = await fetchStrings(settingsDatasheets.find(configDatasheet => configDatasheet.name === 'strings'), template);

    if (!fs.existsSync(_path)) {
      fs.mkdirSync(_path);
    }

    await generateEditionSettings(settingsDatasheets, _path);
    fs.writeFileSync(path.resolve(`${_path}/custom_config.js`), template);
  } catch (err) {
    console.error(err);
  }
};

main();
