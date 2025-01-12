import json
import logging

from ai_shared.config import settings, Env
from ai_shared.databus.ai_po import AiNodeAirTableSetting, AiNodeAiTableSetting
from ai_trainers.loaders.airagent.airtable import AirAgentAirTableLoader, MockAirTable
from ai_trainers.loaders.airagent.aitable import AirAgentAiTableLoader, MockApiTable
from ai_trainers.loaders.attachments_field_loader import AttachmentFieldLoader
from ai_trainers.loaders.pdf_loader import PDFLoader
from ai_trainers.loaders.url_field_loader import UrlFieldLoader
from ai_trainers.loaders.url_loader import URLLoader

logger = logging.getLogger()


def test_url_loader_help_center():
    urls = [
        "https://help.apitable.com/docs/guide/tutorial-1-quick-start",
        "https://help.apitable.com/docs/guide/one-minute-space",
    ]
    for url in urls:
        url_loader = URLLoader(url)
        ds = url_loader.lazy_load()
        assert len(ds.documents) > 3
        assert ds.documents[0].metadata.get("type") == "url"
        assert not ds.documents[0].metadata.get("primary_key")
        # logger.debug(ds.documents)


def test_attachment_local_file_loader():
    # 放个pdf到tests/test.pdf
    file_path = "./tests/test.pdf"
    pdf_loader = PDFLoader(file_path)
    results = pdf_loader.lazy_load()
    for ds in results:
        logger.debug(ds.documents)
        assert len(ds.documents) > 0
        assert ds.documents[0].metadata.get("type") == "pdf"
        assert not ds.documents[0].metadata.get("primary_key")


def test_attachments_field_pdf_loader():
    attach_data = {
            "id": "atcp43KqDc8Nk",
            "name": "sample.pdf",
            "size": 18810,
            "token": "space/2023/09/14/d6c354737371417aaa060895bf69726f",
            "width": 0,
            "bucket": "QNY1",
            "height": 0,
            "preview": "space/2023/09/14/6505c3823be84bf1ae053df8200a6213",
            "mimeType": "application/pdf",
        }
    attach_field_loader = AttachmentFieldLoader(attach_data)
    results = attach_field_loader.lazy_load()
    for ds in results:
        logger.debug(ds.documents)
        assert len(ds.documents) > 0
        assert ds.documents[0].metadata.get("type") == "pdf"
        assert not ds.documents[0].metadata.get("primary_key")


def test_attachments_field_docx_loader():
    attach_data = {
            "id": "atcj27BMYZGON",
            "name": "file-sample_100kB.docx",
            "size": 111303,
            "token": "space/2023/09/21/fcabedd305224f52901b1deacb0e0111",
            "width": 0,
            "bucket": "QNY1",
            "height": 0,
            "mimeType": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        }
    attach_field_loader = AttachmentFieldLoader(attach_data)
    results = attach_field_loader.lazy_load()
    for ds in results:
        logger.debug(ds.documents)
        assert len(ds.documents) > 0
        assert ds.documents[0].metadata.get("type") == "docx"
        assert not ds.documents[0].metadata.get("primary_key")


def test_attachments_field_doc_loader():
    attach_data = {
            "id": "atc6eh4D8rWjm",
            "name": "file-sample_100kB.doc",
            "size": 100352,
            "token": "space/2023/09/21/804d31400d6a4aee9dbfedc0ea49cb75",
            "width": 0,
            "bucket": "QNY1",
            "height": 0,
            "mimeType": "application/msword",
        }
    attach_field_loader = AttachmentFieldLoader(attach_data)
    results = attach_field_loader.lazy_load()
    for ds in results:
        logger.debug(ds.documents)
        assert len(ds.documents) > 0
        assert ds.documents[0].metadata.get("type") == "doc"
        assert not ds.documents[0].metadata.get("primary_key")


def test_attachments_field_markdown_loader():
    attach_data = {
        "id": "atclAifqSeZ9O",
        "name": "markdown-sample.md",
        "size": 3398,
        "token": "space/2023/09/21/a2d93b0455924896990eb68264efed00",
        "width": 0,
        "bucket": "QNY1",
        "height": 0,
        "mimeType": "text/markdown",
    }
    attach_field_loader = AttachmentFieldLoader(attach_data)
    results = attach_field_loader.lazy_load()
    for ds in results:
        logger.debug(ds.documents)
        assert len(ds.documents) > 0
        assert ds.documents[0].metadata.get("type") == "markdown"
        assert not ds.documents[0].metadata.get("primary_key")


def test_url_field_loader():
    url_data = {
        "text": "https://help.apitable.com/docs/guide/tutorial-1-quick-start",
        "title": "https://help.apitable.com/docs/guide/tutorial-1-quick-start",
        "type": 2,
    }
    url_field_loader = UrlFieldLoader(url_data)
    results = url_field_loader.lazy_load()
    for ds in results:
        logger.debug(ds.documents)
        assert len(ds.documents) > 0
        assert ds.documents[0].metadata.get("type") == "url"
        assert not ds.documents[0].metadata.get("primary_key")


def test_airagent_airtable_loader():
    with settings.set_env_airagent():
        AirAgentAirTableLoader.table_cls = MockAirTable

        data = json.loads("""{"baseId": "test", "tableId": "test", "apiKey": "test"}""")
        setting = AiNodeAirTableSetting.parse_obj(data)
        loader = AirAgentAirTableLoader(setting)
        results = loader.lazy_load()
        for ds in results:
            logger.debug(ds.documents)
            assert len(ds.documents) > 0
            assert ds.documents[0].metadata.get("type") == "airtable"
            assert not ds.documents[0].metadata.get("primary_key")


def test_airagent_aitable_loader():
    with settings.set_env_airagent():
        AirAgentAiTableLoader.table_cls = MockApiTable

        data = json.loads("""{"datasheetId": "test", "viewId": "test", "apiKey": "test"}""")
        setting = AiNodeAiTableSetting.parse_obj(data)
        loader = AirAgentAiTableLoader(setting)
        results = loader.lazy_load()
        for ds in results:
            logger.debug(ds.documents)
            assert len(ds.documents) > 0
            assert ds.documents[0].metadata.get("type") == "aitable"
            assert not ds.documents[0].metadata.get("primary_key")