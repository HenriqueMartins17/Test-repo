"""
This script is used to build the Chroma DB file for the AITable Copilot
The db files will saved to folder .data/aitable_copilot-help
"""
import os
import argparse
from ai_trainers.trainers.copilot.vika_copilot import VikaCopilotTrainer
from ai_trainers.trainers.copilot.aitable_copilot import AitableCopilotTrainer, TrainProcessInfoPack
import asyncio
from ai_shared.config import settings, Edition

class CopilotTool:
    def __init__(self):
        self.parser = argparse.ArgumentParser(description="AITable Copilot Builder CLI Tool")
        self.parser.add_argument("-edition", choices=["apitable-saas", "vika-saas"], help="specify the edition of the AI Copilot, aitable: build with documents of aitable help center, vikadata: build with documents of vikadata help center")
        self.parser.add_argument("-action", choices=["load", "train", "build", "all"], help="Choose an action, load: load the .md files, train: vectorize text snippets, build: build the docker image, all: load, train, build")
        self.args = self.parser.parse_args()

        edition_str = os.getenv("EDITION")

        if self.args.edition is not None:
            edition_str = self.args.edition
        
        self.edition = Edition(edition_str)
    
        if self.edition.is_aitable_saas():
            from .config import aitable_config as conf
            tarinerClass = AitableCopilotTrainer
            self.api_user_token = os.getenv("AITABLE_API_USER_TOKEN")
        elif self.edition.is_vika_saas():
            from .config import vika_config as conf
            tarinerClass = VikaCopilotTrainer
            self.api_user_token = os.getenv("VIKA_API_USER_TOKEN")
        else:
            exit("Error: Invalid edition")

        if self.api_user_token is None:
            exit("Error: Please set the environment variable VIKA_API_USER_TOKEN or AITABLE_API_USER_TOKEN.")
        
        self.markdown_paths = conf.get("markdown_paths")
        self.host = conf.get("host")
        self.help_center_host = conf.get("help_center_host")
        self.datasheet_id = conf.get("datasheet_id")
        self.repo = conf.get("repo")
        self.trainer = tarinerClass(self.datasheet_id, self.help_center_host, self.repo, verbose=True)

            
    async def load(self) -> bool:
        """
        Extract content from markdown files and save them to a datasheet
        """
        trainer = self.trainer

        for docs in trainer.load_markdown_files(self.markdown_paths):
            result = trainer.create_new_datasheet(self.api_user_token, docs, host=self.host)
            if result is False:
                return False

        print("loading......ok")
        return True

    async def train(self) -> bool:
        trainer = self.trainer
        new_training_id = TrainProcessInfoPack.make_training_id(trainer.ai_id)
        records = trainer.load_data_sources_via_fusion_api(self.api_user_token, host=self.host)
        
        train_process_info: TrainProcessInfoPack = await trainer.new_train_process_info(new_training_id)
        await trainer.do_train_via_records(train_process_info, records)

        print(f"new_training_id:{new_training_id}")
        
        return True

    async def main(self):
        actions = []
        if self.args.action == "all":
            actions = [self.load, self.train]
        elif self.args.action == "load":
            actions = [self.load]
        elif self.args.action == "train":
            actions = [self.train]
        elif self.args.action == "build":
            pass
        else:
            exit("Error: Invalid action")

        print("The copilot tool is running......")
        for action in actions:
            result = await action()
            if result is False:
                exit("Error: Failed to execute the action: %s" % action.__name__)

        

if __name__ == "__main__":
    tool = CopilotTool()
    asyncio.run(tool.main())
