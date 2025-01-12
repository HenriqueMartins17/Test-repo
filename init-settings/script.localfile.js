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

const https = require('https');
const fs = require('fs');
const path = require('path');
const envfile = require('envfile');

const SPACE_ID = 'spczdmQDfBAn5';
const CONFIG_PACKAGE_ID = 'fodt3Nv5QPPNX';
const BUILD_PATH = './';
const TEMPLATE = fs.readFileSync('./template.txt', 'utf-8');

const sleep = (delay = 1000) => new Promise(res => setTimeout(res, delay));

// Get the location of edition
const getEditionLocation = (editionName) => {
    console.log('edition is ' + editionName);
    return `./l10n-${editionName}`;
};

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

const mergeJsonObjects = (obj1, obj2, clearSubObj = false) => {
    if (clearSubObj) {
        for (let key in obj1) {
            if (obj1.hasOwnProperty(key) && !obj2.hasOwnProperty(key)) {
                console.log('delete sub obj keys:', key)
                delete obj1[key];
            }
        }
    }
    for (let key in obj2) {
        if (obj2.hasOwnProperty(key)) {
            if (obj2[key] instanceof Object && obj1.hasOwnProperty(key) && obj1[key] instanceof Object) {
                obj1[key] = mergeJsonObjects(obj1[key], obj2[key], true);
            } else {
                obj1[key] = obj2[key];
            }
        }
    }
    return obj1;
}

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

const readFileAsJson = (directory, fileName) => {
    const filePath = path.join(directory, fileName);
    try {
        const data = fs.readFileSync(filePath, 'utf8');
        const jsonData = JSON.parse(data);
        return jsonData;
    } catch (err) {
        console.error(`read json file ${directory}-${fileName} error：${err.message}`);
        return null;
    }
}
const readStringsFromFolder = (folderPath) => {
    const files = fs.readdirSync(folderPath);
    const result = {};
    for (const file of files) {
        if (file.endsWith('.json') && file.startsWith('strings')) {
            const filePath = path.join(folderPath, file);
            const fileContent = fs.readFileSync(filePath, 'utf-8');
            const fileJson = JSON.parse(fileContent);
            const locale = file.replace('strings', '')
                .replace('.', '')
                .replace('.json', '');
            result[locale] = fileJson;
        }
    }
    return result;
};

const readLanguageListFromFolder = (folderPath) => {
    const files = fs.readdirSync(folderPath);
    let result = null;
    for (const file of files) {
        if (file.endsWith('.json') && file.startsWith('language.manifest')) {
            const filePath = path.join(folderPath, file);
            const fileContent = fs.readFileSync(filePath, 'utf-8');
            result = JSON.parse(fileContent);
        }
    }
    return result;
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
const fetchPublic = async (location) => {
    const publicFileJson = path.resolve(`${location}/public.json`);
    if (!fs.existsSync(publicFileJson)) {
        console.log(`Public.json not found in ${location}, skip`);
        return;
    }

    const _path = path.resolve(`${BUILD_PATH}/public`);
    if (!fs.existsSync(_path)) {
        fs.mkdirSync(_path);
    }

    fs.readFile(publicFileJson, 'utf8', (err, data) => {
        if (err) throw err;

        const jsonData = JSON.parse(data);

        Object.keys(jsonData).forEach(item => {
            const attachmentFile = jsonData[item]?.file?.[0];

            if (!attachmentFile) {
                console.error(`${item} file has wrong data`);
            }else {
                download(attachmentFile.url, item);
            }
        })

    });
};

// Add private i18n content for files in lang directory
const fetchStrings = async (editionLocation, template) => {
    let _template = template;
    const languageManifest = readLanguageListFromFolder(editionLocation);
    if (!languageManifest) {
        _template = _template.replace('languageList', '');
    } else {
        _template = _template.replace('languageList', `window.apitable_language_list = ${JSON.stringify(languageManifest, null, 2)}`);
    }
    if (!editionLocation) {
        console.log('Strings datasheet not found, skip');
        return _template.replace('languageContent', '');
    }

    console.log(`Execute ${editionLocation} strings to generate custom language pack`);

    const baseLangPacks = {};
    const editionLangPacks = readStringsFromFolder(editionLocation);
    const langPacks = mergeJsonObjects(baseLangPacks, editionLangPacks);
    if (!Object.keys(langPacks).length) {
        console.log('Strings datasheet has no data')
        return _template.replace('languageContent', '');
    }

    const langStrArr = [];
    Object.keys(langPacks).forEach(lang => {
        const dict = langPacks[lang];
        const jsonDict = JSON.stringify(dict, null, 2);
        langStrArr.push(`'${lang}': ${jsonDict}`);
    });
    _template = _template.replace(`languageContent`, langStrArr.join(','));

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

const generateEditionSettings = async (editionLocation, _path) => {
    const baseEnvs = readFileAsJson('./apitable/packages/l10n/base', 'env.json') || {};
    const envs = readFileAsJson(editionLocation, 'env.json') || {};
    const _envs = mergeJsonObjects(baseEnvs, envs);
    let parsedFile = {};

    Object.keys(_envs).forEach((key) => {
        let envValue = _envs[key].value || '';
        if (typeof _envs[key].value !== 'string' && typeof _envs[key].value !== 'undefined') {
            envValue = JSON.stringify(_envs[key].value);
        }
        parsedFile[`${key.toUpperCase()}`] = envValue ?? '';
    });
    console.log('generate env config', parsedFile);
    fs.writeFileSync(path.resolve(`${_path}/.env`), envfile.stringify(parsedFile));
};

const main = async () => {
    try {
        const editionName = getEditionName();
        if (editionName === 'apitable-ce') {
            return;
        }
        const editionLocation = getEditionLocation(editionName);
        console.log(editionName + ' location is in ' + editionLocation);

        // public files
        await fetchPublic(editionLocation);

        let template = TEMPLATE;
        const _path = path.resolve(`${BUILD_PATH}/custom`);
        template = await fetchStrings(editionLocation, template);

        if (!fs.existsSync(_path)) {
            fs.mkdirSync(_path);
        }

        await generateEditionSettings(editionLocation, _path);
        fs.writeFileSync(path.resolve(`${_path}/custom_config.js`), template);
    } catch (err) {
        console.error(err);
    }
};

main();
