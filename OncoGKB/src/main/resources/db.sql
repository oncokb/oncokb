
DROP TABLE IF EXISTS `tumor_type`;
CREATE TABLE IF NOT EXISTS `tumor_type` (
  `tumor_type_id` varchar(25) NOT NULL,
  `name` varchar(255) NOT NULL,
  `short_name` varchar(25) NOT NULL,
  `color` char(31),
  PRIMARY KEY (`tumor_type_id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `short_name` (`short_name`)
) ENGINE=INNODB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

DROP TABLE IF EXISTS `gene`;
CREATE TABLE IF NOT EXISTS `gene` (
  `entrez_gene_id` int(11) NOT NULL,
  `hugo_symbol` varchar(50) NOT NULL,
  `aliases` varchar(500),
  PRIMARY KEY (`entrez_gene_id`),
  UNIQUE KEY `hugo_symbol` (`hugo_symbol`)
) ENGINE=INNODB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

DROP TABLE IF EXISTS `alteration`;
CREATE TABLE IF NOT EXISTS `alteration` (
  `alteration_id` int(11) NOT NULL auto_increment,
  `entrez_gene_id` int(11) NOT NULL,
  `alteration` varchar(30) NOT NULL COMMENT 'V600E, truncating, AMP, DEL',
  `type` varchar(10) NOT NULL COMMENT 'Mutation, CNA',
  PRIMARY KEY (`alteration_id`),
  FOREIGN KEY (`entrez_gene_id`) REFERENCES `gene`(`entrez_gene_id`)
) ENGINE=INNODB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- --------------------------------------------------------

DROP TABLE IF EXISTS `drug`;
CREATE TABLE IF NOT EXISTS `drug` (
  `drug_id` smallint(6) NOT NULL auto_increment,
  `drug_name` varchar(255) NOT NULL,
  `synonyms` varchar(1000),
  `fda_approved` tinyint(1) NOT NULL,
  PRIMARY KEY (`drug_id`),
  UNIQUE KEY `drug_name` (`drug_name`)
) ENGINE=INNODB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- --------------------------------------------------------

DROP TABLE IF EXISTS `evidence`;
CREATE TABLE IF NOT EXISTS `evidence` (
  `evidence_id` int(11) NOT NULL auto_increment,
  `evidence_type` varchar(20) NOT NULL,
  `entrez_gene_id` int(11) NOT NULL,
  `drug_id` smallint(6),
  `tumor_type_id` varchar(25) NOT NULL,
  `pmids` varchar(200) COMMENT 'comma delimited pmids',
  PRIMARY KEY (`evidence_id`),
  FOREIGN KEY (`entrez_gene_id`) REFERENCES `gene`(`entrez_gene_id`),
  FOREIGN KEY (`drug_id`) REFERENCES `drug`(`drug_id`),
  FOREIGN KEY (`tumor_type_id`) REFERENCES `tumor_type`(`tumor_type_id`)
) ENGINE=INNODB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- --------------------------------------------------------