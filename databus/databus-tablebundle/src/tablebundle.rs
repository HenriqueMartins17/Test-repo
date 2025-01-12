use std::fs::{self, File};
use std::io::{BufReader, BufWriter, Read, Write};
use std::path::Path;
use zip::result::ZipResult;
use zip::write::FileOptions;
use zip::ZipWriter;

pub struct TableBundle {}

impl TableBundle {
  pub fn new() -> TableBundle {
    TableBundle {}
  }

  pub fn get_nodes_count(&self) -> u64 {
    0
  }

  #[allow(dead_code)]
  fn save<I, P, Q>(&self, input_paths: I, output_path: Q) -> ZipResult<()>
  where
    I: IntoIterator<Item = P>,
    P: AsRef<Path>,
    Q: AsRef<Path>,
  {
    let output_file = File::create(output_path)?;
    let zip_dest = BufWriter::new(output_file);
    let mut zip = ZipWriter::new(zip_dest);

    for input_path in input_paths {
      let input_path = input_path.as_ref();
      let input_file = File::open(input_path)?;
      let options = FileOptions::default().compression_method(zip::CompressionMethod::Stored);
      let file_name = input_path.file_name().and_then(|os_str| os_str.to_str()).unwrap_or("");
      zip.start_file(file_name, options)?;
      let mut reader = BufReader::new(input_file);
      let mut buffer = Vec::new();
      reader.read_to_end(&mut buffer)?;
      zip.write_all(&buffer)?;
    }

    zip.finish()?;
    Ok(())
  }

  #[allow(dead_code)]
  fn load<T: AsRef<str>, U: AsRef<str>>(&self, zip_path: T, output_dir: U) -> ZipResult<()> {
    let zip_file = File::open(zip_path.as_ref())?;
    let reader = BufReader::new(zip_file);
    let mut archive = zip::ZipArchive::new(reader)?;

    for i in 0..archive.len() {
      let mut file = archive.by_index(i)?;
      let output_path = Path::new(output_dir.as_ref()).join(file.mangled_name());

      if file.is_dir() {
        fs::create_dir_all(&output_path)?;
      } else {
        if let Some(parent) = output_path.parent() {
          if !parent.exists() {
            fs::create_dir_all(&parent)?;
          }
        }

        let mut output_file = File::create(&output_path)?;
        std::io::copy(&mut file, &mut output_file)?;
      }
    }

    Ok(())
  }
}

#[cfg(test)]
#[cfg(test)]
mod tests {
  use super::*;
  use tempfile::tempdir;

  #[test]
  fn test_save_and_load() -> std::io::Result<()> {
    let ta = TableBundle::new();

    // create temporary files for zip
    let file1_contents = b"Hello, world!";
    let temp_dir = tempdir()?;
    let file1_path = temp_dir.path().join("file1.txt");
    let mut file1 = BufWriter::new(File::create(&file1_path)?);
    file1.write_all(file1_contents)?;
    file1.flush()?;

    let file2_contents = b"Testing compression.";
    let file2_path = temp_dir.path().join("file2.txt");
    let mut file2 = BufWriter::new(File::create(&file2_path)?);
    file2.write_all(file2_contents)?;
    file2.flush()?;

    // save files
    let input_files = vec![file1_path.to_str().unwrap(), file2_path.to_str().unwrap()];
    let output_zip = temp_dir.path().join("test_compressed.zip");
    ta.save(&input_files, &output_zip)?;

    // load files
    let output_dir = tempdir()?;
    println!("output_dir:z {:?}", output_dir.path().to_str().unwrap());

    ta.load(output_zip.to_str().unwrap(), output_dir.path().to_str().unwrap())?;

    // verify decompressed files
    let decompressed_file1_path = output_dir.path().join("file1.txt");
    let mut decompressed_file1_contents = Vec::new();
    File::open(&decompressed_file1_path)?.read_to_end(&mut decompressed_file1_contents)?;
    assert_eq!(decompressed_file1_contents, file1_contents);

    let decompressed_file2_path = output_dir.path().join("file2.txt");
    let mut decompressed_file2_contents = Vec::new();
    File::open(&decompressed_file2_path)?.read_to_end(&mut decompressed_file2_contents)?;
    assert_eq!(decompressed_file2_contents, file2_contents);

    // cleanup
    fs::remove_file(file1_path)?;
    fs::remove_file(file2_path)?;
    fs::remove_dir_all(output_dir.path())?;
    fs::remove_file(&output_zip)?;

    Ok(())
  }
}
