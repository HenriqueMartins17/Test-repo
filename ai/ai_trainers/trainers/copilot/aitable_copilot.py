import sys
from ai_shared.llmodel import LlModel
import requests, json, ctypes, multiprocessing, time, traceback, re, datetime, html
from bs4 import BeautifulSoup
from loguru import logger
from typing import Iterator, Optional, List, Union
from ai_shared.persist.data_source import DataSource, count_openai_tokens, count_words_and_characters
from langchain.document_loaders import DirectoryLoader, TextLoader
from ai_trainers.trainers.base import BaseRAGTrainer, TrainProcessInfo
from langchain.docstore.document import Document
from langchain.text_splitter import MarkdownHeaderTextSplitter
from pydantic import BaseModel
from ai_shared.persist.ai_info import AIInfo
from ai_shared.ai_setting import BaseAISetting
from ai_shared.persist.training_info import TrainingInfo, TrainingStatus
from ai_shared.data_source_processor import DataSourceProcessor

class TrainCallback:
    def __init__(self) -> None:
        self.done = multiprocessing.Value(ctypes.c_bool, False)
        self.err = multiprocessing.Value(ctypes.c_bool, False)
        self.training_info: Optional[TrainingInfo] = None

    def set_done(self, training_info: TrainingInfo):
        self.done.value = True
        self.err.value = False
        self.training_info = training_info

    def set_err(self, training_info: TrainingInfo):
        self.done.value = True
        self.err.value = True
        self.training_info = training_info

class TrainProcessInfoPack(BaseModel):
    """
    When we start a train, give back the info about this train info pack,
    includes ai_model(mysql), ai_setting(mysql), ai_info(json), training_info(json)
    """

    ai_id: str
    ai_info: AIInfo  # do not modify this for safe load & save
    new_training_id: str

    ai_model: Optional[dict]
    ai_setting: Optional[BaseAISetting]
    ai_agent_type: str

    async def get_ai_info(self) -> AIInfo:
        return await AIInfo.load_ai_info(self.ai_id)
    
    async def get_training_info(self) -> Union[TrainingInfo, None]:
        return await TrainingInfo.load_training_info(self.ai_id, self.new_training_id)
    
    @staticmethod
    def make_training_id(ai_id: str) -> str:
        """
        Every "train" will have a unique Vector DB store place
        The store folder name is the `training_id`
        Returns the Vector DB's persistent name of the AI.
        """
        now = datetime.datetime.utcnow()  # GMT+0
        formatted_datetime = now.strftime("%Y%m%d%H%M%S")
        return f"{ai_id}_{formatted_datetime}"


class AitableCopilotTrainer(BaseRAGTrainer):
    """
    Train AITable Copilot via help center documents
    Output: ./data/aitable_copilot
    """

    def __init__(self, datasheet_id: str, help_center_host: str, repo_name: str, verbose=False) -> None:
        super().__init__("aitable_copilot")
        self.datasheet_id = datasheet_id
        self.help_center_host = help_center_host
        self.verbose = verbose
        self.repo_name = repo_name

    def load_markdown_files(self, markdown_paths: list[str]) -> Iterator[Document]:
        """
        load all markdown files in folders
        """

        for path in markdown_paths:
            if self.verbose:
                print(f"Loading markdown files from {path}")

            loader = DirectoryLoader(path, glob="**/*.md", loader_cls=TextLoader, show_progress=True)
            docs = loader.load()
            docs = self.split_markdown_by_header(docs, path)

            loader2 = DirectoryLoader(path, glob="**/*.mdx", loader_cls=TextLoader, show_progress=True)
            docs2 = loader2.load()
            docs2 = self.split_markdown_by_header(docs, path)

            docs.extend(docs2)
            #print(docs)
            yield docs
                        


    def split_markdown_by_header(self, docs: list[Document], path: str) -> list[Document]:
        """
        split the markdown content by header1, header2, header3 into different chunks
        """

        headers_to_split_on = [
            ("#", "Header 1"),
            ("##", "Header 2"),
            ("###", "Header 3"),
        ]

        markdown_splitter = MarkdownHeaderTextSplitter(headers_to_split_on=headers_to_split_on)

        all_docs = []

        for doc in docs:
            page_url = self._extract_url_from_slug(doc.page_content, path).strip()
            markdown_content = self._replace_image_url_in_markdown(doc.page_content, page_url) if page_url else doc.page_content

            splits = markdown_splitter.split_text(markdown_content)
            source = page_url if page_url else doc.metadata["source"]
            for index, split in enumerate(splits):
                # Skip any splits that are just the markdown front matter
                if split.page_content.startswith("---") and split.page_content.endswith("---"):
                    continue
                
                if split.page_content.find("sidebar_position") != -1:
                    continue  # skip the sidebar_position

                split.metadata["source"] = source
                split.metadata["chunk_index"] = index
                all_docs.append(split)
                # print(f"--------------------------md_header_split: {index}--------------------------")
                # print(split)
                # print("\n\n\n")
   
        
        return all_docs
    
    def _is_changelog(self, path: str) -> bool:
        """
        check if the path is changelog
        """

        # Substrings to check
        changelogPaths = [f"{self.repo_name}/changelog"]

        # Check if any of the substrings are in docUrl
        return any(changelogPath in path for changelogPath in changelogPaths)
    
    def _extract_url_from_slug(self, split: str, path: str) -> str:
        """
        extract url from the slug value in front matter
        """

        # Regular expression pattern for matching the slug value in front matter
        pattern = r'^slug:\s+(.*)$'

        # search for the matching part using regular expressions
        match = re.search(pattern, split, re.MULTILINE)

        if match:
            slug = match.group(1)
            slug = slug[1:] if slug.startswith("/") else slug

        folder = "changelog" if self._is_changelog(path) else "docs"

        return f"{self.help_center_host}/{folder}/{slug}" if match else ""
    
    def _replace_image_url_in_markdown(self, md_content: str, page_url: str) -> str:
        """
        replace the image local relative path in the markdown content with http url
        """

        image_urls = self._extract_all_images_from_page(page_url)

        for alt_text, src in image_urls.items():
            pattern = r'!\[{}\]\(.*\)'.format(alt_text)

            matches = re.finditer(pattern, md_content)
            for match in matches:

                md_content = md_content.replace(match.group(0), '![{}]({})'.format(alt_text, src))

        return md_content
    
    def _extract_all_images_from_markdown(self, md_content: str) -> dict:
        """
        extract all image tags from the markdown content
        """

        # match the image tag in markdown
        pattern = r'!\[([^\]]*)\]\(([^\)]+)\)'
        matches = re.findall(pattern, md_content)

        image_dict = {}
        for match in matches:
            alt_text, url = match
            image_dict[alt_text] = url

        return image_dict
    
    def _extract_all_images_from_page(self, page_url: str) -> dict:
        """
        extract all image urls from the page url
        """

        image_urls = {}

        # get the page content
        response = requests.get(page_url)
        self.verbose and logger.info(f"Fetching page url: {page_url}")

        if response.status_code == 200:
            # Use BeautifulSoup to parse the web page content
            soup = BeautifulSoup(response.text, 'html.parser')

            # extract the url and alt attributes of the <img> tag
            img_tags = soup.find_all('img')
            
            for img_tag in img_tags:
                img_url = img_tag.get('src')
                img_alt = img_tag.get('alt')
                if img_alt:
                    img_alt = html.unescape(img_alt) # unescape html entities
                    image_urls[img_alt] = f"{self.help_center_host}{img_url}"
        else:
            logger.error(f"Failed to fetch page url: {page_url}")
            logger.error(response)
            sys.exit(1)

        self.verbose and print(f"extracted image urls: {image_urls}\n\n")

        return image_urls
    
    def _create_empty_datasheet(self):
        """
        TODO: create empty datasets via API
        """
        url = f"https://vika.cn/fusion/v1/spaces/{self.spaceId}/datasheets"
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self.api_user_token}"
        }
        pass
    
    def create_new_datasheet(self, api_user_token: str, documents: list[Document], host: Optional[str] = "api.vika.cn") -> bool:
        """
        create new datasets
        """
        url = f"https://{host}/fusion/v1/datasheets/{self.datasheet_id}/records"
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {api_user_token}"
        }
        
        try:
            for i in range(0, len(documents), 5):
                batch = documents[i:i+5]  # get 5 documents per batch
                post_data = []

                for doc in batch:
                    post_data.append({
                        "fields": {
                            "guide_snippet": doc.page_content,
                            "metadata": json.dumps(doc.metadata, ensure_ascii=False),
                        }
                    })

                    #print(post_data)

                response = requests.post(url, headers=headers, json={
                    "fieldKey": "name",
                    "records": post_data
                })
                response.raise_for_status()  # Raise an exception for 4xx and 5xx status codes

                response_json = response.json()
                #print(json.dumps(response_json, indent=2, ensure_ascii=False))

                if response_json["success"] is True:
                    print(f"Batch {i+1}-{i+len(batch)}: created {len(batch)} records.")
                else:
                    raise ValueError(f"Batch {i+1}-{i+len(batch)}: HTTP POST failed. status code: {response.status_code}") 
                        

        except requests.exceptions.RequestException as e:
            print(f"HTTP POST Error：{str(e)}")
            return False
        
        return True


    def load_data_sources(self) -> Iterator[DataSource]:
        """
        Load apitable datasheet with AI_ID from MySQL database as Document object.
        """
        loader = APITableKeyValueLoader(self.ai_id)

        # The id of datasheet which saved all help documents chunks
        # https://integration.aitable.ai/workbench/dst3Xrw76ecadnTRNM/viwz7VBiAP3ej
        dst_ids = [self.datasheet_id]

        return loader.lazy_load(dst_ids)
    
    def load_data_sources_via_fusion_api(self, api_user_token: str, host: Optional[str] = "api.vika.cn") -> Iterator[Document]:
        url = f"https://{host}/fusion/v1/datasheets/{self.datasheet_id}/records"
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {api_user_token}"
        }

        records = []
        page_size = 200
        page_num = 1
        total_records = 0

        try:
            while True:
                print(f"Fetching records in page {page_num} from datasheet...total:{total_records}")
                response = requests.get(url, headers=headers, params={
                    "fieldKey": "name",
                    "pageSize": page_size,
                    "pageNum": page_num
                })

                response.raise_for_status()  # Raise an exception for 4xx and 5xx status codes

                response_json = response.json()
                #print(json.dumps(response_json, indent=2, ensure_ascii=False))

                if response_json["success"] is True:
                    total_records = response_json["data"]["total"]
                    records.extend(response_json["data"]["records"])

                    if len(records) >= total_records:
                        break  # All records retrieved
                    else:
                        page_num += 1
                else:
                    raise ValueError(f"HTTP POST failed. status code: {response.status_code}")

        except requests.exceptions.RequestException as e:
            print(f"HTTP POST Error：{str(e)}")

        finally:
            #print(json.dumps(records, indent=2, ensure_ascii=False))
            return records


    async def new_train_process_info(self, new_training_id: str) -> TrainProcessInfoPack:
        ai_po: dict = {}
        ai_setting: BaseAISetting = None
        ai_info: AIInfo
        
        if not AIInfo.exist(self.ai_id):
            ai_info = await AIInfo.new(self.ai_id)
        else:
            ai_info = await AIInfo.load_ai_info(self.ai_id)

        embedding_model = LlModel.get_embedding_model_by_model(None)
        print(f"embedding_model: {embedding_model}")
        training_info = TrainingInfo(ai_id=ai_info.ai_id, training_id=new_training_id, embedding_model=embedding_model)
        await training_info.save()

        
        train_process_info: TrainProcessInfoPack = TrainProcessInfoPack(
            new_training_id=new_training_id,
            ai_id=self.ai_id,
            ai_info=ai_info,
            ai_model=ai_po,
            ai_setting=ai_setting,
            ai_agent_type=self.ai_id, # vika_copilot
        )

        print("train_process_info:")
        print(train_process_info)
        return train_process_info
    
    async def do_train_via_records(
        self,
        train_process_info_pack: TrainProcessInfoPack,
        records: List[dict],
        callbacks: Optional[List[TrainCallback]] = None,
    ):
        """
        Train with the training ID you provided
        """
        ai_info = await train_process_info_pack.get_ai_info()
        new_training_id = train_process_info_pack.new_training_id
        training_info = await ai_info.get_training_info(new_training_id)
        logger.info(f"AI: {self.ai_id} Training {new_training_id} started.....")

        try:
            # load AI Info from .data
            await ai_info.lock(new_training_id)

            training_info.status = TrainingStatus.TRAINING
            training_info.started_at = int(time.time())
            await training_info.save()

            # start training
            docs: List[Document] = []

            for record in records:
                page_content = ""
                metadata = json.loads(record["fields"]["metadata"]) if "metadata" in record["fields"] else {}
                   
                for field_name, field_value in record["fields"].items():
                    if field_name.startswith("."):
                        key = field_name.replace(".", "")
                        metadata[key] = field_value
                    elif field_name != "metadata":
                        page_content += f"{field_name}: {field_value}\n"

                doc = Document(
                    page_content=page_content,
                    metadata=metadata,
                )

                docs.append(doc)

                # words, characters, tokens count
                content_words_count, content_characters_count = count_words_and_characters(
                    page_content
                )
                content_tokens_count = count_openai_tokens(page_content)

                metadata["words"] = content_words_count
                metadata["characters"] = content_characters_count
                metadata["tokens"] = content_tokens_count


            persist_path = TrainingInfo.make_training_path(ai_id=self.ai_id, training_id=new_training_id)
            print(training_info)
            embedding_model = training_info.get_embedding_model()

            db = await DataSourceProcessor.embedding(
              training_persist_path=persist_path,
              embedding_model=embedding_model,
              docs=docs,
              verbose=True
            )

            training_info.status = TrainingStatus.SUCCESS
            # finished training

            # how many time(seconds) it takes?
            take_time = int(time.time()) - training_info.started_at
            training_info.info = (
                f"AI: {self.ai_id} Training {new_training_id} take {take_time}s."
            )
            await training_info.save()

            if callbacks:
                for callback in callbacks:
                    callback.set_done(training_info)
        except Exception as e:
            logger.error(traceback.format_exc())

            training_info.status = TrainingStatus.FAILED
            training_info.info = str(e)
            await training_info.save()
        else:
            training_info = await self._training_succeed(train_process_info_pack, [], [])
            if callbacks:
                for callback in callbacks:
                    callback.set_err(training_info)
        finally:
            logger.success(f"_do_train_with_process_info: AI: {self.ai_id} Training {new_training_id} end.....")
