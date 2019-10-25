import XML_Classes.*;
import org.apache.commons.codec.digest.DigestUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class XMLFetcher
{
	
	private MagitRepository m_MagitRepository;
	
	public MagitRepository getMagitRepository()
	{
		return m_MagitRepository;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// search for magit-objects
	
	private MagitSingleFolder searchForFolder(String folderID)
	{
		List<MagitSingleFolder> magitFolders = m_MagitRepository.getMagitFolders().getMagitSingleFolder();
		return magitFolders.stream().filter(i -> i.getId().equals(folderID)).findFirst().orElse(null);
	}
	
	private MagitBlob searchForBlob(String blobID)
	{
		List<MagitBlob> magitBlobs = m_MagitRepository.getMagitBlobs().getMagitBlob();
		return magitBlobs.stream().filter(i -> i.getId().equals(blobID)).findFirst().orElse(null);
	}
	
	private MagitSingleCommit searchForCommit(String commitID)
	{
		List<MagitSingleCommit> magitCommits = m_MagitRepository.getMagitCommits().getMagitSingleCommit();
		return magitCommits.stream().filter(i -> i.getId().equals(commitID)).findFirst().orElse(null);
	}
	
	private MagitSingleBranch searchForBranch(String branchName)
	{
		List<MagitSingleBranch> magitBranches = m_MagitRepository.getMagitBranches().getMagitSingleBranch();
		return magitBranches.stream().filter(i -> i.getName().equals(branchName)).findFirst().orElse(null);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// pre validate XML
	
	public Utilz.TwoParametersResult preValidateXML(String path) throws JAXBException
	{
		Utilz.TwoParametersResult isXML;
		
		isXML = this.isXMLFile(path);
		if(!Utilz.isExists(path))
		{
			return new Utilz.TwoParametersResult(false,"Path does not exists");
		}
		else if (!isXML.m_IsValid)
		{
			return isXML;
		}
		
		unMarshalXMLData(path);
		
		return new Utilz.TwoParametersResult(true, "XML pre-validation is valid");
	}
	
	public Utilz.TwoParametersResult isXMLFile(String XMLPath)
	{
		if (!XMLPath.endsWith("xml"))
		{
			return new Utilz.TwoParametersResult(false, "The file needs to be of type XML");
		}
		
		return new Utilz.TwoParametersResult(true, "The file is of type XML");
	}
	
	public void unMarshalXMLData(String path) throws JAXBException
	{
		File file = new File(path);
		JAXBContext context = JAXBContext.newInstance(MagitRepository.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		m_MagitRepository = (MagitRepository) unmarshaller.unmarshal(file);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// XML validation
	
	public Utilz.TwoParametersResult checkValidationXML() throws JAXBException
	{
		Utilz.TwoParametersResult result = twoElementsWithSameID();
		if (!result.m_IsValid)
		{
			return result;
		}
		
		Utilz.TwoParametersResult checkIfFolderPointValid = folderPointToObject();
		if (!checkIfFolderPointValid.m_IsValid)
		{
			return checkIfFolderPointValid;
		}
		
		Utilz.TwoParametersResult checkIfCommitPointToRoot = commitPointToRoot();
		if (!checkIfCommitPointToRoot.m_IsValid)
		{
			return checkIfCommitPointToRoot;
		}
		
		Utilz.TwoParametersResult checkIfBranchPointToCommit = branchToCommit();
		if (!checkIfBranchPointToCommit.m_IsValid)
		{
			return checkIfBranchPointToCommit;
		}
		
		Utilz.TwoParametersResult checkIfHeadPointToActiveBranch = HeadToBranch();
		if (!checkIfHeadPointToActiveBranch.m_IsValid)
		{
			return checkIfHeadPointToActiveBranch;
		}
		
		return new Utilz.TwoParametersResult(true, "The XML file is valid!");
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// validation 3.2
	
	private Utilz.TwoParametersResult twoElementsWithSameID()
	{
		int count;
		
		List<MagitBlob> magitBlobList = m_MagitRepository.getMagitBlobs().getMagitBlob();
		for (MagitBlob magitBlob : magitBlobList)
		{
			count = 0;
			for (MagitBlob magBlob : magitBlobList)
			{
				if (magitBlob.getId().equals(magBlob.getId()))
				{
					++count;
				}
				if (count > 1)
				{
					return new Utilz.TwoParametersResult(false, "There are two MagitBlobs with the ID '" + magitBlob.getId() + "'");
				}
			}
		}
		
		List<MagitSingleCommit> magitCommitList = m_MagitRepository.getMagitCommits().getMagitSingleCommit();
		for (MagitSingleCommit magitCommit : magitCommitList)
		{
			count = 0;
			for (MagitSingleCommit magCommit : magitCommitList)
			{
				if (magitCommit.getId().equals(magCommit.getId()))
				{
					++count;
				}
				if (count > 1)
				{
					return new Utilz.TwoParametersResult(false, "There are two MagitCommits with the ID '" + magitCommit.getId() + "'");
				}
			}
		}
		
		List<MagitSingleFolder> magitFolderList = m_MagitRepository.getMagitFolders().getMagitSingleFolder();
		for (MagitSingleFolder magitFolder : magitFolderList)
		{
			count = 0;
			for (MagitSingleFolder magFolder : magitFolderList)
			{
				if (magitFolder.getId().equals(magFolder.getId()))
				{
					++count;
				}
				if (count > 1)
				{
					return new Utilz.TwoParametersResult(false, "There are two MagitFolders with the ID '" + magitFolder.getId() + "'");
				}
			}
		}
		
		return new Utilz.TwoParametersResult(true, "All the elements have valid ID's");
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// validation 3.3,3.4,3.5
	
	private Utilz.TwoParametersResult folderPointToObject()
	{
		MagitSingleFolder magitFold;
		MagitBlob magitBlob;
		
		for (MagitSingleFolder magitFolder : m_MagitRepository.getMagitFolders().getMagitSingleFolder())
		{
			for (Item magitItem : magitFolder.getItems().getItem())
			{
				if (magitItem.getType().equals("folder"))
				{
					magitFold = searchForFolder(magitItem.getId());
					if (magitFold == null)
					{
						return new Utilz.TwoParametersResult(false, "The XML file is invalid! Folder '" + magitFolder.getName() + "' does not point to the Folders it should.");
					}
					else if (magitItem.getId().equals(magitFolder.getId()))
					{
						return new Utilz.TwoParametersResult(false, "The XML file is invalid! Folder '" + magitFolder.getName() + "' points to itself.");
					}
				}
				else if (magitItem.getType().equals("blob"))
				{
					magitBlob = searchForBlob(magitItem.getId());
					if (magitBlob == null)
					{
						return new Utilz.TwoParametersResult(false, "The XML file is invalid! Folder '" + magitFolder.getName() + "' does not point to the Blobs it should.");
					}
				}
			}
		}
		
		return new Utilz.TwoParametersResult(true, "The Folders point to valid objects");
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// validation 3.6,3.7
	
	private Utilz.TwoParametersResult commitPointToRoot()
	{
		MagitSingleFolder magitFolder;
		
		for (MagitSingleCommit magitCommit : m_MagitRepository.getMagitCommits().getMagitSingleCommit())
		{
			magitFolder = searchForFolder(magitCommit.getRootFolder().getId());
			if (magitFolder == null)
			{
				return new Utilz.TwoParametersResult(false, "A Commit does not point to a Folder");
			}
			else if (!magitFolder.isIsRoot())
			{
				return new Utilz.TwoParametersResult(false, "A Commit does not point to a Root Folder");
			}
		}
		
		return new Utilz.TwoParametersResult(true, "All the Commits point to the Root Folders they should");
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// validation 3.8
	
	private Utilz.TwoParametersResult branchToCommit()
	{
		MagitSingleCommit magitCommit;
		int amountOfCommits = this.m_MagitRepository.getMagitCommits().getMagitSingleCommit().size();
		
		for (MagitSingleBranch magitBranch : m_MagitRepository.getMagitBranches().getMagitSingleBranch())
		{
			magitCommit = searchForCommit(magitBranch.getPointedCommit().getId());
			if (magitCommit == null && amountOfCommits != 0)
			{
				return new Utilz.TwoParametersResult(false, "The Branch '" + magitBranch.getName() + "' does not point to a Commit");
			}
		}
		
		return new Utilz.TwoParametersResult(true, "All the Branches point to the Commits they should");
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// validation 3.9
	
	private Utilz.TwoParametersResult HeadToBranch()
	{
		MagitSingleBranch magitBranch;
		String nameOfActiveBranch = m_MagitRepository.getMagitBranches().getHead();
		magitBranch = searchForBranch(nameOfActiveBranch);
		
		if (magitBranch == null)
		{
			return new Utilz.TwoParametersResult(false, "The head points to none existing Branch");
		}
		
		return new Utilz.TwoParametersResult(true, "The active branch is valid");
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// validation utilz
	public Utilz.TwoParametersResult isValidPath()
	{
		return Utilz.isValidPath(this.getMagitRepository().getLocation());
	}
	
	public boolean isEmpty()
	{
		return Utilz.isEmpty(this.m_MagitRepository.getLocation());
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// fetch of XML data
	
	public Repository FetchXMLData() throws IOException
	{
		Repository repository;
		Head head;
		Branch branch;
		String commitSHA1;
		
		head = new Head(m_MagitRepository.getMagitBranches().getHead());
		repository = new Repository(head, m_MagitRepository.getLocation(), m_MagitRepository.getName());
		repository.createMagit(repository.getPath());
		repository.createMagitFile("Head", head.getBranchName(), "branch");
		repository.createMagitFile("Repository", repository.getRepositoryName(), "branch");
		
		for (MagitSingleBranch magitBranch : m_MagitRepository.getMagitBranches().getMagitSingleBranch())
		{
			MagitSingleCommit magitCommit = searchForCommit(magitBranch.getPointedCommit().getId());
			commitSHA1 = magitCommit==null ? "null": getCommitChain(magitCommit, repository);
			branch = new Branch(magitBranch.getName(), commitSHA1);
			repository.addBranch(branch.getName(), branch);
			repository.createMagitFile(branch.getName(), branch.getKeyOfCommit(), "branch");
		}
		
		return repository;
	}
	
	private String getCommitChain(MagitSingleCommit magitCommit, Repository repository) throws IOException
	{
		String commitSHA1;
		
		MagitSingleFolder magitRootFolder = searchForFolder(magitCommit.getRootFolder().getId());
		NodeInformation info = fetchInRecursion(magitRootFolder, repository);
		Folder rootFolder = (Folder) repository.getFiles().get(info.getEncryption());
		rootFolder.setTypeOfFolder(Folder.eRoot.Root);
		if (magitCommit.getPrecedingCommits() == null || magitCommit.getPrecedingCommits().getPrecedingCommit().isEmpty())
		{
			Commit commit = new Commit(info.getEncryption(), "null", magitCommit.getMessage(), magitCommit.getDateOfCreation(), magitCommit.getAuthor());
			commitSHA1 = commit.CommitSHA1();
			repository.addCommit(commitSHA1, commit);
			repository.createMagitFile(commitSHA1, commit.commitToFileString(), "object");
		}
		else
		{
			int size = magitCommit.getPrecedingCommits().getPrecedingCommit().size();
			String previousCommitID = magitCommit.getPrecedingCommits().getPrecedingCommit().get(size - 1).getId();
//			for(PrecedingCommits.PrecedingCommit precedeCommit : magitCommit.getPrecedingCommits().getPrecedingCommit())
//			{
			String previousCommitSHA1 = getCommitChain(searchForCommit(previousCommitID), repository);
			Commit commit = new Commit(info.getEncryption(), previousCommitSHA1, magitCommit.getMessage(), magitCommit.getDateOfCreation(), magitCommit.getAuthor());
			commitSHA1 = commit.CommitSHA1();
			repository.addCommit(commitSHA1, commit);
			repository.createMagitFile(commitSHA1, commit.commitToFileString(), "object");
		}
		
		return commitSHA1;
	}
	
	private NodeInformation fetchInRecursion(Object node, Repository repository) throws IOException
	{
		NodeInformation nodeInformation = null;
		String SHA1="";
		if (node instanceof MagitSingleFolder)
		{
			Map<String,NodeInformation> nodeInformationMap = new TreeMap<>();
			NodeInformation info;
			for (Item folderSon : ((MagitSingleFolder) node).getItems().getItem())
			{
				if (folderSon.getType().equals("blob"))
				{
					info=fetchInRecursion(searchForBlob(folderSon.getId()), repository);
				}
				else
				{
					info=fetchInRecursion(searchForFolder(folderSon.getId()), repository);
				}
				
				nodeInformationMap.putIfAbsent(info.getEncryption(),info);
			}
			
			SHA1 = Folder.folderSHA1(nodeInformationMap);
			Folder folder = new Folder(Folder.eRoot.Regular, nodeInformationMap);
			repository.addFile(SHA1, folder);
			//creating a Folder file
			String folderContent = folder.folderToFileString();
			repository.createMagitFile(SHA1, folderContent, "object");
			nodeInformation = new NodeInformation(((MagitSingleFolder) node).getName(), SHA1,
					((MagitSingleFolder) node).getLastUpdater(), NodeInformation.ItemType.Folder, ((MagitSingleFolder) node).getLastUpdateDate());
		}
		else if (node instanceof MagitBlob)
		{
			SHA1 = DigestUtils.sha1Hex(((MagitBlob) node).getContent());
			repository.addFile(SHA1, new Blob(((MagitBlob) node).getContent()));
			//creating a Blob file
			repository.createMagitFile(SHA1, ((MagitBlob) node).getContent(), "object");
			nodeInformation = new NodeInformation(((MagitBlob) node).getName(), SHA1,
					((MagitBlob) node).getLastUpdater(), NodeInformation.ItemType.Blob, ((MagitBlob) node).getLastUpdateDate());
		}
		
		return nodeInformation;
	}
}

