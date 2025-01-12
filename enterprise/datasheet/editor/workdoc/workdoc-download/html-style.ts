export const htmlStyles = `
<style>
:root {
  --textCommonPrimary: rgba(51, 51, 51, 1);
  --rainbowPurple5: rgba(123, 103, 238, 1);
  --rainbowOrange5: rgba(255, 166, 42, 1);
  --rainbowTeal5: rgba(25, 215, 151, 1);
  --rainbowGray5: rgba(181, 181, 181, 1);
  --rainbowBrown5: rgba(191, 124, 76, 1);
  --rainbowRed5: rgba(238, 83, 71, 1);
  --rainbowIndigo5: rgba(86, 132, 245, 1);
  --rainbowPink5: rgba(255, 98, 127, 1);
  --bgCommonDefault: rgba(26, 26, 26, 1);
}
.ProseMirror {
    width: 840px;
    margin: 24px auto;
}
.ProseMirror ul[data-type='taskList'] {
  padding-left: 0;
}
.ProseMirror ul[data-type='taskList'] li {
  display: flex;
  padding: 0;
  align-items: center;
}
.ProseMirror ul[data-type='taskList'] li label {
  margin-right: 8px;
  user-select: none;
  align-self: flex-start;
  padding-top: 10px;
  position: relative;
}
.ProseMirror ul[data-type='taskList'] li[data-checked='true'] {
  text-decoration: line-through;
}
.ProseMirror ul[data-type='taskList'] li input {
  display: block;
  width: 16px;
  height: 16px;
}
.ProseMirror ul[data-type='taskList'] li input[type='checkbox'] {
  opacity: 0;
  position: absolute;
}
.ProseMirror ul[data-type='taskList'] li input[type='checkbox'] + span {
  position: relative;
  top: 0;
  left: 0;
  cursor: pointer;
  display: block;
  width: 16px;
  height: 16px;
  background-color: rgba(255, 255, 255, 1);
  border: 1.5px solid rgba(156, 156, 156, 1);
  border-radius: 4px;
  transition: all 0.36s;
}
.ProseMirror ul[data-type='taskList'] li input[type='checkbox']:checked + span {
  background-color: rgba(123, 103, 238, 1);
  border-color: rgba(123, 103, 238, 1);
}
.ProseMirror ul[data-type='taskList'] li input[type='checkbox']:checked + span:after {
  position: absolute;
  top: 45%;
  left: 20%;
  width: 5px;
  height: 10px;
  display: table;
  border: 2px solid #fff;
  border-top: 0;
  border-left: 0;
  transform: rotate(45deg) scale(1) translate(-50%,-50%);
  opacity: 1;
  transition: all 0.2s cubic-bezier(0.12,0.4,0.29,1.46) 0.1s;
  content: ' ';
}
.ProseMirror ul[data-type='taskList'] li > div {
  min-width: 100px;
}
.ProseMirror pre {
    margin: 8px 4px;
    padding: 16px;
    border-radius: 4px;
    background-color: #f5f5f5;
}
.ProseMirror blockquote {
    margin: 4px 0;
    border-left: 2px solid #e8e8e8;
    padding-left: 14px;
    padding-right: 4px;
    min-height: 24px;
    color: #9c9c9c;
}
.ProseMirror code {
    background-color: rgba(51, 51, 51, 0.12);
    color: #696969;
    padding: 2px 4px;
    border-radius: 2px;
}
.ProseMirror ol {
    margin-top: 0;
    margin-bottom: 0;
    padding-left: 16px;
    list-style-type: none;
    counter-reset: num;
}
.ProseMirror ol li {
    min-height: 24px;
    margin: 0 4px;
    position: relative;
    counter-increment: num;
}
.ProseMirror ol li::before {
    color: #7b67ee;
    content: counter(num) '.';
    position: absolute;
    padding: 4px 0;
    height: 32px;
    right: 100%;
}
.ProseMirror ol li {
    min-height: 24px;
    margin: 0 4px;
    position: relative;
    counter-increment: num;
}
.ProseMirror ol li > ol > li {
    counter-increment: count;
}
.ProseMirror ol li > ol > li::before {
    content: counter(count, lower-alpha) '.';
}
.ProseMirror ol li > ol > li > ol {
    list-style-type: none;
    counter-reset: listCounter;
}
.ProseMirror ol li > ol > li > ol > li {
    counter-increment: listCounter;
}
.ProseMirror ol li > ol > li > ol > li::before {
    content: counter(listCounter, upper-roman) '.';
}
.ProseMirror ul {
    margin: 0;
    padding-left: 16px;
    list-style-position: inside;
    list-style-type: none;
}
.ProseMirror p, .ProseMirror h1, .ProseMirror h2, .ProseMirror h3, .ProseMirror h4, .ProseMirror h5, .ProseMirror h6 {
    min-height: 24px;
    line-height: 24px;
    padding: 4px;
    margin: 0;
}
.ProseMirror h1 {
  margin-top: 20px;
  font-size: 28px;
  line-height: 42px;
}
.ProseMirror h2 {
  margin-top: 16px;
  font-size: 24px;
  line-height: 36px;
}
.ProseMirror h3 {
  margin-top: 12px;
  font-size: 20px;
  line-height: 30px;
}
.ProseMirror h4 {
  margin-top: 8px;
  font-size: 18px;
  line-height: 28px;
}
.ProseMirror h5,
.ProseMirror h6 {
  font-size: 16px;
  line-height: 24px;
}
.ProseMirror pre code {
    color: #9c9c9c;
    background: none;
    padding: 0;
}
</style>
`;