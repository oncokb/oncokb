
# drop in reverse order of create to satisfy the foreign key constraint.
DROP TABLE IF EXISTS `gene_evidence`;
DROP TABLE IF EXISTS `alteration_activity_evidence`;
DROP TABLE IF EXISTS `drug_sensitivity_evidence`;
DROP TABLE IF EXISTS `alteration`;
DROP TABLE IF EXISTS `drug`;
DROP TABLE IF EXISTS `gene`;
DROP TABLE IF EXISTS `tumor_type`;

-- --------------------------------------------------------

CREATE TABLE `tumor_type` (
  `tumor_type_id` varchar(25) NOT NULL,
  `name` varchar(255) NOT NULL,
  `short_name` varchar(25) NOT NULL,
  `color` char(31),
  PRIMARY KEY (`tumor_type_id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `short_name` (`short_name`)
) ENGINE=INNODB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

CREATE TABLE `gene` (
  `entrez_gene_id` int(11) NOT NULL,
  `hugo_symbol` varchar(50) NOT NULL,
  `aliases` varchar(500),
  PRIMARY KEY (`entrez_gene_id`),
  UNIQUE KEY `hugo_symbol` (`hugo_symbol`)
) ENGINE=INNODB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

CREATE TABLE `drug` (
  `drug_id` int(11) NOT NULL auto_increment,
  `drug_name` varchar(255) NOT NULL,
  `synonyms` varchar(1000),
  `fda_approved` tinyint(1) NOT NULL,
  PRIMARY KEY (`drug_id`),
  UNIQUE KEY `drug_name` (`drug_name`)
) ENGINE=INNODB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- --------------------------------------------------------

CREATE TABLE `alteration` (
  `alteration_id` int(11) NOT NULL auto_increment,
  `entrez_gene_id` int(11) NOT NULL,
  `alteration` varchar(30) NOT NULL COMMENT 'V600E, truncating, AMP, DEL',
  `type` varchar(10) NOT NULL COMMENT 'Mutation, CNA',
  PRIMARY KEY (`alteration_id`),
  FOREIGN KEY (`entrez_gene_id`) REFERENCES `gene`(`entrez_gene_id`)
) ENGINE=INNODB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- --------------------------------------------------------

CREATE TABLE `gene_evidence` (
  `evidence_id` int(11) NOT NULL auto_increment,
  `conclusion` varchar(20) NOT NULL COMMENT 'tumor suppressor gene, oncogene',
  `entrez_gene_id` int(11) NOT NULL,
  `tumor_type_id` varchar(25) DEFAULT NULL,
  `pmids` varchar(200) COMMENT 'comma delimited pmids',
  `comments` text,
  PRIMARY KEY (`evidence_id`),
  FOREIGN KEY (`entrez_gene_id`) REFERENCES `gene`(`entrez_gene_id`),
  FOREIGN KEY (`tumor_type_id`) REFERENCES `tumor_type`(`tumor_type_id`)
) ENGINE=INNODB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- --------------------------------------------------------

CREATE TABLE `alteration_activity_evidence` (
  `evidence_id` int(11) NOT NULL auto_increment,
  `conclusion` varchar(20) NOT NULL COMMENT 'gain-of-function, loss-of-function, switch_of_function',
  `downstream_effect` varchar(1000) COMMENT 'e.g. AKT activation',
  `context` varchar(1000) COMMENT 'coregulated events',
  `alteration_id` int(11) NOT NULL,
  `tumor_type_id` varchar(25) DEFAULT NULL,
  `pmids` varchar(200) COMMENT 'comma delimited pmids',
  `comments` text,
  PRIMARY KEY (`evidence_id`),
  FOREIGN KEY (`alteration_id`) REFERENCES `alteration`(`alteration_id`),
  FOREIGN KEY (`tumor_type_id`) REFERENCES `tumor_type`(`tumor_type_id`)
) ENGINE=INNODB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- --------------------------------------------------------

CREATE TABLE `drug_sensitivity_evidence` (
  `evidence_id` int(11) NOT NULL auto_increment,
  `conclusion` varchar(20) NOT NULL COMMENT 'sensitive, insensitive',
  `context` varchar(1000) COMMENT 'coregulated events',
  `alteration_id` int(11) NOT NULL,
  `drug_id` int(11),
  `tumor_type_id` varchar(25) DEFAULT NULL,
  `pmids` varchar(200) COMMENT 'comma delimited pmids',
  `comments` text,
  PRIMARY KEY (`evidence_id`),
  FOREIGN KEY (`alteration_id`) REFERENCES `alteration`(`alteration_id`),
  FOREIGN KEY (`drug_id`) REFERENCES `drug`(`drug_id`),
  FOREIGN KEY (`tumor_type_id`) REFERENCES `tumor_type`(`tumor_type_id`)
) ENGINE=INNODB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- --------------------------------------------------------